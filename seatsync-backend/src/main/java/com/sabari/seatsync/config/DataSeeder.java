package com.sabari.seatsync.config;

import com.sabari.seatsync.dto.EventRequest;
import com.sabari.seatsync.dto.EventResponse;
import com.sabari.seatsync.dto.ShowRequest;
import com.sabari.seatsync.entity.User;
import com.sabari.seatsync.entity.UserRole;
import com.sabari.seatsync.repository.EventRepository;
import com.sabari.seatsync.repository.UserRepository;
import com.sabari.seatsync.service.EventService;
import com.sabari.seatsync.service.ShowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** LOCAL DEVELOPMENT ONLY. Creates the admin account, a demo user, and sample events/shows on first start. */
@Component
@ConditionalOnProperty(name = "seatsync.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final EventRepository events;
    private final EventService eventService;
    private final ShowService showService;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataSeeder(UserRepository users, EventRepository events, EventService eventService, ShowService showService,
                      PasswordEncoder encoder, @Value("${seatsync.admin.email}") String adminEmail,
                      @Value("${seatsync.admin.password}") String adminPassword) {
        this.users = users; this.events = events; this.eventService = eventService; this.showService = showService;
        this.encoder = encoder; this.adminEmail = adminEmail; this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!users.existsByEmail(adminEmail)) {
            users.save(new User("Admin", adminEmail, encoder.encode(adminPassword), UserRole.ADMIN));
        }
        if (!users.existsByEmail("user@seatsync.local")) {
            users.save(new User("Demo User", "user@seatsync.local", encoder.encode("User@123"), UserRole.USER));
        }
        if (events.count() > 0) return;

        String[][] data = {
            {"Avengers: Secret Wars", "Earth's mightiest heroes face their biggest multiverse threat yet.", "PVR Cinemas Chennai", "165"},
            {"Interstellar (Re-release)", "A team of explorers travels through a wormhole in search of a new home.", "INOX Marina Mall", "169"},
            {"Coldplay Tribute Live", "A live tribute band playing two hours of stadium singalongs.", "Nehru Indoor Stadium", "120"},
            {"Stand-up Night: Chennai Laughs", "Four comics, one stage, zero filter.", "Phoenix Marketcity", "90"}
        };
        LocalTime[][] slots = {{LocalTime.of(10, 0), LocalTime.of(12, 45)}, {LocalTime.of(14, 0), LocalTime.of(16, 45)},
                               {LocalTime.of(19, 0), LocalTime.of(21, 45)}};
        for (String[] d : data) {
            EventResponse e = eventService.create(new EventRequest(d[0], d[1], d[2], Integer.parseInt(d[3])));
            for (int day = 1; day <= 2; day++) {
                for (LocalTime[] s : slots) {
                    showService.create(new ShowRequest(e.id(), LocalDate.now().plusDays(day), s[0], s[1], 4, 5, new BigDecimal("250")));
                }
            }
        }
        log.warn("Seeded LOCAL DEVELOPMENT data. admin={} / user=user@seatsync.local (passwords are in application.yml / README)", adminEmail);
    }
}
