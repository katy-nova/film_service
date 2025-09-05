package com.example.films.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.films.model.enums.FriendshipStatus;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "friendship")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;

    private Instant createdAt = Instant.now();
    private Instant updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;


    public Friendship(final User user, final User friend, final FriendshipStatus status) {
        this.user = user;
        this.friend = friend;
        this.status = status;
    }
}
