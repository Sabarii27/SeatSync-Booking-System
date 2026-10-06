package com.sabari.seatsync.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "seats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"show_id", "row_label", "seat_number"}))
public class Seat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "show_id")
    private Show show;
    @Column(nullable = false, length = 3)
    private String rowLabel;
    @Column(nullable = false)
    private Integer seatNumber;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;
    @Column(name = "held_by")
    private Long heldBy;
    private Instant holdExpiresAt;

    protected Seat() {}
    public Seat(Show show, String rowLabel, Integer seatNumber, BigDecimal price) {
        this.show = show; this.rowLabel = rowLabel; this.seatNumber = seatNumber; this.price = price;
    }

    public void hold(Long userId, Instant until) { status = SeatStatus.HELD; heldBy = userId; holdExpiresAt = until; }
    public void book() { status = SeatStatus.BOOKED; heldBy = null; holdExpiresAt = null; }
    public void release() { status = SeatStatus.AVAILABLE; heldBy = null; holdExpiresAt = null; }
    public boolean isHoldExpired(Instant now) { return holdExpiresAt == null || !holdExpiresAt.isAfter(now); }
    public String getLabel() { return rowLabel + seatNumber; }

    public Long getId() { return id; }
    public Show getShow() { return show; }
    public String getRowLabel() { return rowLabel; }
    public Integer getSeatNumber() { return seatNumber; }
    public BigDecimal getPrice() { return price; }
    public SeatStatus getStatus() { return status; }
    public Long getHeldBy() { return heldBy; }
    public Instant getHoldExpiresAt() { return holdExpiresAt; }
    public void setHoldExpiresAt(Instant t) { holdExpiresAt = t; }
}
