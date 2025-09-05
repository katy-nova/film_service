package com.example.films.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.films.dto.user.UserSimpleDto;
import com.example.films.model.entity.Friendship;
import io.swagger.v3.oas.annotations.Hidden;

import java.util.Optional;

@Hidden
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findById(Long id);

    @Query("SELECT f FROM Friendship f WHERE (f.user.id = :userId AND f.friend.id = :friendId)" +
            " OR (f.user.id = :friendId AND f.friend.id = :userId)")
    Optional<Friendship> findByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query("SELECT new com.example.films.dto.user.UserSimpleDto(u.name, u.login) FROM User u" +
            " WHERE EXISTS (SELECT 1 FROM Friendship f WHERE " +
            "(f.user.id = :userId AND f.friend.id = u.id OR f.friend.id = :userId AND f.user.id = u.id)" +
            " AND f.status = 'ACCEPTED') AND u.id != :userId ORDER BY u.id ASC")
    Page<UserSimpleDto> findFriendsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new com.example.films.dto.user.UserSimpleDto(u.name, u.login) FROM User u WHERE EXISTS " +
            "(SELECT 1 FROM Friendship f WHERE f.friend.id = :userId AND f.user.id = u.id AND f.status = 'REQUESTED')" +
            "ORDER BY u.id ASC")
    Page<UserSimpleDto> findFollowersByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new com.example.films.dto.user.UserSimpleDto(u.name, u.login) FROM User u WHERE EXISTS " +
            "(SELECT 1 FROM Friendship f WHERE f.user.id = :userId AND f.friend.id = u.id AND f.status = 'REQUESTED')" +
            "ORDER BY u.id ASC")
    Page<UserSimpleDto> findFollowingByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new com.example.films.dto.user.UserSimpleDto(u.name, u.login) FROM User u WHERE EXISTS " +
            "(SELECT 1 FROM Friendship f WHERE f.user.id = :userId AND f.friend.id = u.id AND f.status = 'BLOCKED')" +
            "ORDER BY u.id ASC")
    Page<UserSimpleDto> findBlockedUsers(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new com.example.films.dto.user.UserSimpleDto(u.name, u.login) FROM User u " +
            "WHERE (u IN (SELECT f.friend FROM Friendship f WHERE " +
            "f.user.id =:userId1 AND f.status = 'ACCEPTED') OR u IN " +
            "(SELECT f.user FROM Friendship f WHERE f.friend.id = :userId1 AND f.status = 'ACCEPTED'))" +
            "AND (u IN (SELECT f.friend FROM Friendship f WHERE f.user.id = :userId2 AND f.status = 'ACCEPTED') OR u IN" +
            "(SELECT f.user FROM Friendship f WHERE f.friend.id = :userId2 AND f.status = 'ACCEPTED'))")
    Page<UserSimpleDto> findCommonFriends(@Param("userId1") Long userId1, @Param("userId2") Long userId2, Pageable pageable);
}
