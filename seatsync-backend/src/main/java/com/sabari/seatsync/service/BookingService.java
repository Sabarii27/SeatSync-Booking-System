package com.sabari.seatsync.service;

import com.sabari.seatsync.dto.BookingResponse;
import com.sabari.seatsync.entity.*;
import com.sabari.seatsync.exception.*;
import com.sabari.seatsync.kafka.BookingEvent;
import com.sabari.seatsync.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BookingService {
    private final BookingRepository bookings;
    private final BookingSeatRepository bookingSeats;
    private final SeatRepository seats;
    private final ShowRepository shows;
    private final UserRepository users;
    private final ApplicationEventPublisher publisher;

    public BookingService(BookingRepository bookings, BookingSeatRepository bookingSeats, SeatRepository seats,
                          ShowRepository shows, UserRepository users, ApplicationEventPublisher publisher) {
        this.bookings = bookings; this.bookingSeats = bookingSeats; this.seats = seats;
        this.shows = shows; this.users = users; this.publisher = publisher;
    }

    /**
     * Confirm held seats. One transaction:
     *  1. lock the seat rows (SELECT ... FOR UPDATE)   2. re-check state on the fresh rows
     *  3. HELD -> BOOKED, create Booking + BookingSeats, total computed from DB prices   4. commit
     * The Kafka event is published only AFTER the commit (see BookingEventProducer).
     */
    @Transactional
    public BookingResponse confirm(Long userId, Long showId, List<Long> seatIds) {
        List<Long> ids = seatIds.stream().distinct().sorted().toList();
        Show show = shows.findWithEventById(showId).orElseThrow(() -> new ResourceNotFoundException("Show not found"));
        User user = users.findById(userId).orElseThrow(() -> new UnauthorizedException("User no longer exists"));

        List<Seat> locked = seats.lockByIds(ids);
        if (locked.size() != ids.size()) throw new ResourceNotFoundException("One or more seats do not exist");

        Instant now = Instant.now();
        for (Seat s : locked) {
            if (!s.getShow().getId().equals(showId)) throw new InvalidBookingException("Seat " + s.getLabel() + " does not belong to this show");
            boolean heldByMeAndValid = s.getStatus() == SeatStatus.HELD && userId.equals(s.getHeldBy()) && !s.isHoldExpired(now);
            if (heldByMeAndValid) continue;
            boolean takenBySomeoneElse = s.getStatus() == SeatStatus.BOOKED
                    || (s.getStatus() == SeatStatus.HELD && !userId.equals(s.getHeldBy()) && !s.isHoldExpired(now));
            if (takenBySomeoneElse) throw new SeatUnavailableException("Seat " + s.getLabel() + " is no longer available");
            throw new SeatHoldExpiredException("Your hold on seat " + s.getLabel() + " has expired. Please select your seats again.");
        }

        BigDecimal total = locked.stream().map(Seat::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        Booking booking = bookings.save(new Booking(user, show, BookingStatus.CONFIRMED, total));
        for (Seat s : locked) {
            s.book();
            bookingSeats.save(new BookingSeat(booking, s, s.getPrice()));
        }
        publisher.publishEvent(new BookingEvent("BOOKING_CONFIRMED", booking.getId(), userId, showId, ids, total, Instant.now()));
        return toResponse(booking, labels(locked));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listForUser(Long userId) { return toResponses(bookings.findByUserDetailed(userId)); }

    @Transactional(readOnly = true)
    public List<BookingResponse> listAll() { return toResponses(bookings.findAllDetailed()); }

    @Transactional(readOnly = true)
    public BookingResponse get(Long userId, Long bookingId) {
        Booking b = bookings.findDetailedById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!b.getUser().getId().equals(userId)) throw new ForbiddenException("This booking belongs to another user");
        return toResponses(List.of(b)).get(0);
    }

    /**
     * Cancellation rule: only the owner, only CONFIRMED bookings, and only before the show starts.
     * Seats go back to AVAILABLE.
     */
    @Transactional
    public BookingResponse cancel(Long userId, Long bookingId) {
        Booking b = bookings.lockById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!b.getUser().getId().equals(userId)) throw new ForbiddenException("This booking belongs to another user");
        if (b.getStatus() != BookingStatus.CONFIRMED) throw new InvalidBookingException("Only confirmed bookings can be cancelled");
        Show show = b.getShow();
        if (!LocalDateTime.of(show.getShowDate(), show.getStartTime()).isAfter(LocalDateTime.now()))
            throw new InvalidBookingException("This show has already started, so the booking can no longer be cancelled");
        releaseAndMark(b, BookingStatus.CANCELLED, "BOOKING_CANCELLED");
        return toResponses(List.of(b)).get(0);
    }

    /** Admin rule: CONFIRMED can move to CANCELLED or EXPIRED (seats are released). Nothing else is allowed. */
    @Transactional
    public BookingResponse adminChangeStatus(Long bookingId, BookingStatus target) {
        Booking b = bookings.lockById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (b.getStatus() != BookingStatus.CONFIRMED || (target != BookingStatus.CANCELLED && target != BookingStatus.EXPIRED))
            throw new InvalidBookingException("Only a CONFIRMED booking can be changed to CANCELLED or EXPIRED");
        releaseAndMark(b, target, target == BookingStatus.CANCELLED ? "BOOKING_CANCELLED" : "BOOKING_EXPIRED");
        return toResponses(List.of(b)).get(0);
    }

    private void releaseAndMark(Booking b, BookingStatus status, String eventType) {
        List<BookingSeat> links = bookingSeats.findByBookingIdIn(List.of(b.getId()));
        List<Long> seatIds = links.stream().map(l -> l.getSeat().getId()).sorted().toList();
        seats.lockByIds(seatIds).forEach(Seat::release);
        b.setStatus(status);
        publisher.publishEvent(new BookingEvent(eventType, b.getId(), b.getUser().getId(), b.getShow().getId(),
                seatIds, b.getTotalAmount(), Instant.now()));
    }

    private List<BookingResponse> toResponses(List<Booking> list) {
        if (list.isEmpty()) return List.of();
        Map<Long, List<Seat>> byBooking = new HashMap<>();
        bookingSeats.findByBookingIdIn(list.stream().map(Booking::getId).toList())
                .forEach(bs -> byBooking.computeIfAbsent(bs.getBooking().getId(), k -> new ArrayList<>()).add(bs.getSeat()));
        return list.stream().map(b -> toResponse(b, labels(byBooking.getOrDefault(b.getId(), List.of())))).toList();
    }

    private static List<String> labels(List<Seat> seatList) {
        return seatList.stream()
                .sorted(Comparator.comparing(Seat::getRowLabel).thenComparing(Seat::getSeatNumber))
                .map(Seat::getLabel).toList();
    }

    private static BookingResponse toResponse(Booking b, List<String> seatLabels) {
        Show s = b.getShow();
        return new BookingResponse(b.getId(), s.getEvent().getTitle(), s.getEvent().getVenue(), s.getId(), s.getShowDate(),
                s.getStartTime(), seatLabels, b.getTotalAmount(), b.getStatus(), b.getCreatedAt(), b.getUser().getEmail());
    }
}
