package com.sabari.seatsync.controller;

import com.sabari.seatsync.dto.UserDto;
import com.sabari.seatsync.security.AuthUser;
import com.sabari.seatsync.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final UserService userService;
    public ProfileController(UserService userService) { this.userService = userService; }

    @GetMapping
    public UserDto profile(@AuthenticationPrincipal AuthUser user) { return userService.profile(user.id()); }
}
