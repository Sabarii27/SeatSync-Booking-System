package com.sabari.seatsync.service;

import com.sabari.seatsync.dto.*;
import com.sabari.seatsync.entity.User;
import com.sabari.seatsync.entity.UserRole;
import com.sabari.seatsync.exception.ConflictException;
import com.sabari.seatsync.exception.UnauthorizedException;
import com.sabari.seatsync.repository.UserRepository;
import com.sabari.seatsync.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwtService) {
        this.users = users; this.encoder = encoder; this.jwtService = jwtService;
    }

    /** Public registration can only ever create USER accounts (the request has no role field). */
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new ConflictException("That email is already registered");
        User user = users.save(new User(req.name().trim(), email, encoder.encode(req.password()), UserRole.USER));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!encoder.matches(req.password(), user.getPassword())) throw new UnauthorizedException("Invalid email or password");
        return toResponse(user);
    }

    private AuthResponse toResponse(User u) { return new AuthResponse(jwtService.generate(u), UserService.toDto(u)); }
}
