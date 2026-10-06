package com.sabari.seatsync.service;

import com.sabari.seatsync.dto.UserDto;
import com.sabari.seatsync.entity.User;
import com.sabari.seatsync.exception.ResourceNotFoundException;
import com.sabari.seatsync.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository users;
    public UserService(UserRepository users) { this.users = users; }

    public UserDto profile(Long userId) {
        return users.findById(userId).map(UserService::toDto).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    public List<UserDto> listAll() { return users.findAll().stream().map(UserService::toDto).toList(); }

    public static UserDto toDto(User u) { return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole().name(), u.getCreatedAt()); }
}
