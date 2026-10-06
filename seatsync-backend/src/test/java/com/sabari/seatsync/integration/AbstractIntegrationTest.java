package com.sabari.seatsync.integration;

import com.sabari.seatsync.dto.EventRequest;
import com.sabari.seatsync.dto.ShowRequest;
import com.sabari.seatsync.dto.ShowResponse;
import com.sabari.seatsync.entity.User;
import com.sabari.seatsync.entity.UserRole;
import com.sabari.seatsync.kafka.BookingEventProducer;
import com.sabari.seatsync.repository.*;
import com.sabari.seatsync.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Runs against a REAL PostgreSQL started by Testcontainers (Docker must be running). Kafka is switched off. */
@SpringBootTest(properties = {
        "seatsync.seed.enabled=false",
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.admin.auto-create=false"})
@Testcontainers
abstract class AbstractIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @MockitoBean BookingEventProducer producer;

    @Autowired UserRepository users;
    @Autowired EventRepository events;
    @Autowired ShowRepository shows;
    @Autowired SeatRepository seats;
    @Autowired BookingRepository bookings;
    @Autowired BookingSeatRepository bookingSeats;
    @Autowired EventService eventService;
    @Autowired ShowService showService;
    @Autowired SeatService seatService;
    @Autowired BookingService bookingService;

    @BeforeEach
    void cleanDatabase() {
        bookingSeats.deleteAll();
        bookings.deleteAll();
        seats.deleteAll();
        shows.deleteAll();
        events.deleteAll();
        users.deleteAll();
    }

    protected User newUser(String name) {
        return users.save(new User(name, name.toLowerCase() + "@test.com", "hash", UserRole.USER));
    }

    /** A show with 2 rows x 5 seats (10 seats) at 250 each, tomorrow evening. */
    protected ShowResponse newShow() {
        var event = eventService.create(new EventRequest("Test Movie", "d", "Test Venue", 120));
        return showService.create(new ShowRequest(event.id(), LocalDate.now().plusDays(1),
                LocalTime.of(19, 0), LocalTime.of(21, 0), 2, 5, new BigDecimal("250")));
    }
}
