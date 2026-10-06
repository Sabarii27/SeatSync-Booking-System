package com.sabari.seatsync.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Simulated notification service: it just logs what a real email/SMS worker would act on. */
@Component
public class BookingEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);

    @KafkaListener(topics = "${seatsync.kafka.topic}")
    public void handle(BookingEvent e) {
        switch (e.eventType()) {
            case "BOOKING_CONFIRMED" -> log.info("Booking confirmed: {} (user {}, show {}, seats {}, total {})",
                    e.bookingId(), e.userId(), e.showId(), e.seatIds(), e.totalAmount());
            case "BOOKING_CANCELLED" -> log.info("Booking cancelled: {} (seats {} released)", e.bookingId(), e.seatIds());
            default -> log.info("Booking event {}: {}", e.eventType(), e.bookingId());
        }
    }
}
