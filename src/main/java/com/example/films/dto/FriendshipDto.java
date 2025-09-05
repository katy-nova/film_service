package com.example.films.dto;

import lombok.Data;
import com.example.films.model.enums.FriendshipStatus;

import java.time.Instant;

@Data
public class FriendshipDto {
    private String userLogin;
    private String friendLogin;
    private FriendshipStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
