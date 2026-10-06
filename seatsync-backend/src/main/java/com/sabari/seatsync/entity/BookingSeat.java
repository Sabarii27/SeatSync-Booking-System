package com.sabari.seatsync.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "booking_seats")
public class BookingSeat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "booking_id")
    private Booking booking;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "seat_id")
    private Seat seat;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    protected BookingSeat() {}
    public BookingSeat(Booking booking, Seat seat, BigDecimal price) {
        this.booking = booking; this.seat = seat; this.price = price;
    }
    public Long getId() { return id; }
    public Booking getBooking() { return booking; }
    public Seat getSeat() { return seat; }
    public BigDecimal getPrice() { return price; }
}
