package com.sabari.seatsync.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "shows")
public class Show {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "event_id")
    private Event event;
    @Column(nullable = false)
    private LocalDate showDate;
    @Column(nullable = false)
    private LocalTime startTime;
    @Column(nullable = false)
    private LocalTime endTime;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Show() {}
    public Show(Event event, LocalDate showDate, LocalTime startTime, LocalTime endTime) {
        this.event = event; this.showDate = showDate; this.startTime = startTime; this.endTime = endTime;
    }
    @PrePersist void onCreate() { createdAt = Instant.now(); }

    public void reschedule(LocalDate d, LocalTime s, LocalTime e) { showDate = d; startTime = s; endTime = e; }
    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public LocalDate getShowDate() { return showDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
}
