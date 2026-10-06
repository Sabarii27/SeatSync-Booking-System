package com.sabari.seatsync.service;

import com.sabari.seatsync.dto.*;
import com.sabari.seatsync.entity.User;
import com.sabari.seatsync.entity.UserRole;
import com.sabari.seatsync.exception.ConflictException;
import com.sabari.seatsync.exception.UnauthorizedException;
import com.sabari.seatsync.repository.UserRepository;
import com.sabari.seatsync.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository users;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwt;
    @InjectMocks AuthService service;

    @Test
    void registerAlwaysCreatesHashedUserAccount() {
        when(users.existsByEmail("ann@x.com")).thenReturn(false);
        when(encoder.encode("secret1")).thenReturn("HASH");
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(jwt.generate(any())).thenReturn("token");

        AuthResponse res = service.register(new RegisterRequest("Ann", "Ann@X.com", "secret1"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertEquals(UserRole.USER, saved.getValue().getRole());
        assertEquals("HASH", saved.getValue().getPassword());
        assertEquals("ann@x.com", saved.getValue().getEmail());
        assertEquals("token", res.token());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(users.existsByEmail("ann@x.com")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.register(new RegisterRequest("Ann", "ann@x.com", "secret1")));
        verify(users, never()).save(any());
    }

    @Test
    void loginFailsWithWrongPassword() {
        User u = new User("Ann", "ann@x.com", "HASH", UserRole.USER);
        when(users.findByEmail("ann@x.com")).thenReturn(Optional.of(u));
        when(encoder.matches("bad", "HASH")).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> service.login(new LoginRequest("ann@x.com", "bad")));
    }

    @Test
    void loginSucceedsWithCorrectPassword() {
        User u = new User("Ann", "ann@x.com", "HASH", UserRole.USER);
        when(users.findByEmail("ann@x.com")).thenReturn(Optional.of(u));
        when(encoder.matches("good12", "HASH")).thenReturn(true);
        when(jwt.generate(u)).thenReturn("token");
        assertEquals("token", service.login(new LoginRequest("ann@x.com", "good12")).token());
    }
}
