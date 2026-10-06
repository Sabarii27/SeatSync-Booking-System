package com.sabari.seatsync.service;

import com.sabari.seatsync.dto.EventRequest;
import com.sabari.seatsync.entity.Event;
import com.sabari.seatsync.exception.ConflictException;
import com.sabari.seatsync.exception.ResourceNotFoundException;
import com.sabari.seatsync.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    @Mock EventRepository events;
    @Mock ShowRepository shows;
    @Mock SeatRepository seats;
    @Mock BookingRepository bookings;
    @InjectMocks EventService service;

    @Test
    void createSavesAndReturnsEvent() {
        when(events.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));
        var res = service.create(new EventRequest(" Avengers ", "desc", "PVR", 150));
        assertEquals("Avengers", res.title());
        assertEquals(150, res.duration());
    }

    @Test
    void getMissingEventThrowsNotFound() {
        when(events.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.get(9L));
    }

    @Test
    void deleteIsBlockedWhenBookingsExist() {
        when(events.findById(1L)).thenReturn(Optional.of(new Event("T", "d", "V", 100)));
        when(bookings.existsByShowEventId(1L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.delete(1L));
        verify(events, never()).deleteById(any());
    }
}
