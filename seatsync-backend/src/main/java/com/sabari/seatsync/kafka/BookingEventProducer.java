package com.sabari.seatsync.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publishes to Kafka only AFTER the booking transaction has committed, and never throws.
 * A Kafka outage therefore cannot roll back or corrupt a booking.
 * Known limitation: if the app crashes between commit and send, the event is lost (a transactional outbox would fix that).
 */
@Component
public class BookingEventProducer {
    private static final Logger log = LoggerFactory.getLogger(BookingEventProducer.class);

    private final KafkaTemplate<String, BookingEvent> kafka;
    private final String topic;

    public BookingEventProducer(KafkaTemplate<String, BookingEvent> kafka, @Value("${seatsync.kafka.topic}") String topic) {
        this.kafka = kafka; this.topic = topic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingEvent(BookingEvent event) {
        try {
            kafka.send(topic, String.valueOf(event.bookingId()), event).whenComplete((res, ex) -> {
                if (ex != null) log.error("Could not publish {} for booking {}", event.eventType(), event.bookingId(), ex);
                else log.info("Published {} for booking {}", event.eventType(), event.bookingId());
            });
        } catch (Exception ex) {
            log.error("Kafka unavailable, skipped {} for booking {}", event.eventType(), event.bookingId(), ex);
        }
    }
}
