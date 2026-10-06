package com.sabari.seatsync.service;

import com.sabari.seatsync.dto.HoldResponse;
import com.sabari.seatsync.dto.SeatResponse;
import com.sabari.seatsync.entity.Seat;
import com.sabari.seatsync.entity.SeatStatus;
import com.sabari.seatsync.exception.*;
import com.sabari.seatsync.repository.SeatRepository;
import com.sabari.seatsync.repository.ShowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class SeatService {
    private static final Logger log = LoggerFactory.getLogger(SeatService.class);
    static final int MAX_SEATS_PER_HOLD = 8;

    private final SeatRepository seats;
    private final ShowRepository shows;
    private final Duration holdDuration;

    public SeatService(SeatRepository seats, ShowRepository shows, @Value("${seatsync.hold-minutes:5}") long holdMinutes) {
        this.seats = seats; this.shows = shows; this.holdDuration = Duration.ofMinutes(holdMinutes);
    }

    /** What the user sees. An expired hold is shown as AVAILABLE even before the scheduler cleans it up. */
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeats(Long showId, Long currentUserId) {
        if (!shows.existsById(showId)) throw new ResourceNotFoundException("Show not found");
        Instant now = Instant.now();
        return seats.findByShowIdOrderByRowLabelAscSeatNumberAsc(showId).stream().map(s -> {
            SeatStatus effective = effectiveStatus(s, now);
            boolean mine = effective == SeatStatus.HELD && currentUserId != null && currentUserId.equals(s.getHeldBy());
            return new SeatResponse(s.getId(), s.getLabel(), s.getRowLabel(), s.getSeatNumber(), effective, s.getPrice(),
                    mine, mine ? s.getHoldExpiresAt() : null);
        }).toList();
    }

    /**
     * Hold = AVAILABLE -> HELD for N minutes. The row lock (lockByIds) makes the "check then change" atomic:
     * two users racing for the same seat are serialised by PostgreSQL, so only one passes the check.
     */
    @Transactional
    public HoldResponse hold(Long userId, Long showId, List<Long> seatIds) {
        List<Long> ids = seatIds.stream().distinct().sorted().toList();
        if (ids.size() > MAX_SEATS_PER_HOLD) throw new InvalidBookingException("You can hold at most " + MAX_SEATS_PER_HOLD + " seats at once");
        if (!shows.existsById(showId)) throw new ResourceNotFoundException("Show not found");

        List<Seat> locked = seats.lockByIds(ids);
        if (locked.size() != ids.size()) throw new ResourceNotFoundException("One or more seats do not exist");

        Instant now = Instant.now();
        for (Seat s : locked) {
            if (!s.getShow().getId().equals(showId)) throw new InvalidBookingException("Seat " + s.getLabel() + " does not belong to this show");
            if (!canHold(s, userId, now)) throw new SeatUnavailableException("Seat " + s.getLabel() + " is no longer available");
        }
        Instant until = now.plus(holdDuration);
        locked.forEach(s -> s.hold(userId, until));
        return new HoldResponse(ids, until);
    }

    /** Safety net run by the scheduler: HELD with a passed expiry goes back to AVAILABLE. */
    @Transactional
    public int releaseExpiredHolds() {
        int released = seats.releaseExpired(Instant.now());
        if (released > 0) log.info("Released {} expired seat holds", released);
        return released;
    }

    private static boolean canHold(Seat s, Long userId, Instant now) {
        return switch (s.getStatus()) {
            case AVAILABLE -> true;
            case HELD -> s.isHoldExpired(now) || userId.equals(s.getHeldBy());
            case BOOKED -> false;
        };
    }

    private static SeatStatus effectiveStatus(Seat s, Instant now) {
        if (s.getStatus() == SeatStatus.HELD && s.isHoldExpired(now)) return SeatStatus.AVAILABLE;
        return s.getStatus();
    }
}
