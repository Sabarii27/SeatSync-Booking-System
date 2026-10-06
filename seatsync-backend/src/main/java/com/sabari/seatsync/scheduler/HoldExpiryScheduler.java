package com.sabari.seatsync.scheduler;

import com.sabari.seatsync.service.SeatService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HoldExpiryScheduler {
    private final SeatService seatService;
    public HoldExpiryScheduler(SeatService seatService) { this.seatService = seatService; }

    @Scheduled(fixedDelayString = "${seatsync.hold-cleanup-interval-ms:30000}")
    public void releaseExpiredHolds() { seatService.releaseExpiredHolds(); }
}
