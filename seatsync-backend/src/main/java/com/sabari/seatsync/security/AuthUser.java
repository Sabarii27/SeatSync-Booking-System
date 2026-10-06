package com.sabari.seatsync.security;

import com.sabari.seatsync.entity.UserRole;

/** The logged-in user, rebuilt from the JWT on every request (no DB lookup). */
public record AuthUser(Long id, String email, UserRole role) {}
