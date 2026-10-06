package com.sabari.seatsync.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "events")
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(length = 2000)
    private String description;
    @Column(nullable = false)
    private String venue;
    @Column(nullable = false)
    private Integer duration;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    private Instant updatedAt;

    protected Event() {}
    public Event(String title, String description, String venue, Integer duration) {
        this.title = title; this.description = description; this.venue = venue; this.duration = duration;
    }
    @PrePersist void onCreate() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

    public void update(String title, String description, String venue, Integer duration) {
        this.title = title; this.description = description; this.venue = venue; this.duration = duration;
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getVenue() { return venue; }
    public Integer getDuration() { return duration; }
}
