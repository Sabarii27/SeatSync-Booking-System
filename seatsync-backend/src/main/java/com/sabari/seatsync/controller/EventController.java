package com.sabari.seatsync.controller;

import com.sabari.seatsync.dto.EventResponse;
import com.sabari.seatsync.dto.ShowResponse;
import com.sabari.seatsync.service.EventService;
import com.sabari.seatsync.service.ShowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;
    private final ShowService showService;
    public EventController(EventService eventService, ShowService showService) {
        this.eventService = eventService; this.showService = showService;
    }

    @GetMapping
    public List<EventResponse> list(@RequestParam(required = false) String q) { return eventService.list(q); }

    @GetMapping("/{id}")
    public EventResponse get(@PathVariable Long id) { return eventService.get(id); }

    @GetMapping("/{id}/shows")
    public List<ShowResponse> shows(@PathVariable Long id) { return showService.forEvent(id); }
}
