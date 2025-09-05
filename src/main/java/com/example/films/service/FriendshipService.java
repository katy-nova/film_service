package com.example.films.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.films.dto.FriendshipDto;
import com.example.films.dto.mapping.FriendshipMapping;
import com.example.films.dto.user.UserSimpleDto;
import com.example.films.exception.AlreadyExistsException;
import com.example.films.exception.NotFoundException;
import com.example.films.model.entity.Friendship;
import com.example.films.model.entity.User;
import com.example.films.model.enums.FriendshipStatus;
import com.example.films.model.enums.Role;
import com.example.films.repository.FriendshipRepository;
import com.example.films.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FriendshipService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendshipMapping friendshipMapping;

    @Cacheable(cacheNames = "shortUserCache", keyGenerator = "usersPageKeyGenerator")
    public Page<UserSimpleDto> getFriendsByUserId(Long userId, Pageable pageable) {
        return friendshipRepository.findFriendsByUserId(userId, pageable);
    }

    @Cacheable(cacheNames = "shortUserCache", keyGenerator = "usersPageKeyGenerator")
    public Page<UserSimpleDto> getFollowersByUserId(Long userId, Pageable pageable) {
        return friendshipRepository.findFollowersByUserId(userId, pageable);
    }

    @Cacheable(cacheNames = "shortUserCache", keyGenerator = "usersPageKeyGenerator")
    public Page<UserSimpleDto> getFollowingByUserId(Long userId, Pageable pageable) {
        return friendshipRepository.findFollowingByUserId(userId, pageable);
    }

    @Cacheable(cacheNames = "shortUserCache", keyGenerator = "usersPageKeyGenerator")
    public Page<UserSimpleDto> getBlackListByUserId(Long userId, Pageable pageable) {
        return friendshipRepository.findBlockedUsers(userId, pageable);
    }

    public Page<UserSimpleDto> getCommonFriends(Long userId1, Long userId2, Pageable pageable) {
        return friendshipRepository.findCommonFriends(userId1, userId2, pageable);
    }

    @Transactional
    @CacheEvict(cacheNames = "shortUserCache", allEntries = true)
    public FriendshipDto sendRequest(Long userId, Long friendId) {
        Optional<Friendship> maybeFriendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId);
        if (maybeFriendship.isPresent()) {
            Friendship friendship = maybeFriendship.get();
            String message;
            switch (friendship.getStatus()) {
                case ACCEPTED:
                    message = "Пользователь уже добавлен в друзья";
                    break;
                case REQUESTED:
                    if (friendship.getUser().getId().equals(userId)) {
                        message = "Заявка уже отправлена";
                    } else if (friendship.getUser().getId().equals(friendId)) {
                        return acceptRequest(userId, friendId);
                    } else {
                        message = "Непредвиденная ошибка";
                    }
                    break;
                case BLOCKED:
                    message = "Невозможно отправить заявку пользователю, который вас заблокировал";
                    break;
                default:
                    message = "Непредвиденная ошибка";
            }
            log.error(message);
            throw new AlreadyExistsException(message);
        }
        User user = findById(userId);
        User friend = findById(friendId);
        Friendship friendship = new Friendship(user, friend, FriendshipStatus.REQUESTED);
        friendshipRepository.save(friendship);
        return friendshipMapping.toFriendshipDto(friendship);
    }

    //может ли этот метод быть актуальным? чтобы сразу пробрасывалось айди запроса и его было легче найти в базе
    @Transactional
    @CacheEvict(cacheNames = "shortUserCache", allEntries = true)
    public FriendshipDto acceptRequest(Long userId, Long friendId, Long requestId) {
        Friendship friendship = findFriendshipById(requestId);
        if (friendship.getUser().getId().equals(friendId) && friendship.getFriend().getId().equals(userId)
                && friendship.getStatus().equals(FriendshipStatus.REQUESTED)) {
            friendship.setStatus(FriendshipStatus.ACCEPTED);
            friendship.setUpdatedAt(Instant.now());
            friendshipRepository.save(friendship);
            return friendshipMapping.toFriendshipDto(friendship);
        } else {
            String message = "Отправлен некорректный запрос";
            log.error(message);
            throw new RuntimeException(message);
        }
    }

    @Transactional
    @CacheEvict(cacheNames = "shortUserCache", allEntries = true)
    public FriendshipDto acceptRequest(Long userId, Long friendId) {
        Friendship friendship = findFriendship(userId, friendId);
        if (friendship.getUser().getId().equals(friendId) && friendship.getFriend().getId().equals(userId)
                && friendship.getStatus().equals(FriendshipStatus.REQUESTED)) {
            friendship.setStatus(FriendshipStatus.ACCEPTED);
            friendship.setUpdatedAt(Instant.now());
            friendshipRepository.save(friendship);
            return friendshipMapping.toFriendshipDto(friendship);
        } else {
            String message = "Отправлен некорректный запрос";
            log.error(message);
            throw new RuntimeException(message);
        }
    }

    @Transactional
    @CacheEvict(cacheNames = "shortUserCache", allEntries = true)
    public FriendshipDto addToBlackList(Long userId, Long friendId) {
        User friend = findById(friendId);
        if (isAdmin(friend)) {
            String message = "Нельзя заблокировать администратора";
            log.error(message);
            throw new IllegalStateException(message);
        }
        Optional<Friendship> maybeFriendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId);
        if (maybeFriendship.isPresent()) {
            Friendship friendship = maybeFriendship.get();
            if (friendship.getUser().getId().equals(friendId)) {
                friendship.setUser(findById(userId)); //меняем направленность, чтобы понимать кто кого заблокировал
                friendship.setFriend(findById(friendId));
            }
            friendship.setStatus(FriendshipStatus.BLOCKED);
            friendship.setUpdatedAt(Instant.now());
            friendshipRepository.save(friendship);
            return friendshipMapping.toFriendshipDto(friendship);
        }
        // если дружбы не было - создаем новую запись
        User user = findById(userId);
        Friendship friendship1 = new Friendship(user, friend, FriendshipStatus.BLOCKED);
        friendshipRepository.save(friendship1);
        return friendshipMapping.toFriendshipDto(friendship1);
    }

    @Transactional
    @CacheEvict(cacheNames = "shortUserCache", allEntries = true)
    public FriendshipDto removeFromFriends(Long userId, Long friendId) {
        Friendship friendship = findFriendship(userId, friendId);
        if (!friendship.getStatus().equals(FriendshipStatus.ACCEPTED)) {
            String message = "Вы не можете удалить из друзей пользователя, который не является вашим другом";
            log.error(message);
            throw new IllegalStateException(message);
        }
        if (friendship.getUser().getId().equals(userId)) {
            friendship.setUser(findById(friendId)); //меняем направленность, чтобы понимать кто на кого подписан
            friendship.setFriend(findById(userId));
        }
        friendship.setStatus(FriendshipStatus.REQUESTED);
        friendship.setUpdatedAt(Instant.now());
        friendshipRepository.save(friendship);
        return friendshipMapping.toFriendshipDto(friendship);
    }

    // при удалении человека из черного списка стирается запись о дружбе
    @Transactional
    @CacheEvict(cacheNames = "shortUserCache", allEntries = true)
    public void removeFromBlackList(Long userId, Long friendId) {
        Friendship friendship = findFriendship(userId, friendId);
        if (!friendship.getStatus().equals(FriendshipStatus.BLOCKED)) {
            String message = String.format("Пользователь с id: %d не заблокирован пользователем с id: %d", friendId, userId);
            log.warn(message);
            return;
        }
        if (friendship.getUser().getId().equals(friendId)) {
            String message = "Убрать блокировку может только ее инициатор";
            log.error(message);
            throw new IllegalStateException(message);
        }
        friendshipRepository.delete(friendship);
    }

    private Friendship findFriendship(Long userId, Long friendId) {
        return friendshipRepository.findByUserIdAndFriendId(userId, friendId).orElseThrow(() -> {
                    String message = String.format("Связь между пользователями с id: %s, %s не найдена", userId, friendId);
                    log.error(message);
                    return new NotFoundException(message);
                }
        );
    }

    private Friendship findFriendshipById(Long friendshipId) {
        return friendshipRepository.findById(friendshipId).orElseThrow(() -> {
                    String message = String.format("Заявка с id: %s не найдена", friendshipId);
                    log.error(message);
                    return new NotFoundException(message);
                }
        );
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Пользователь с id '%s' не найден", id);
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                });
    }

    private boolean isAdmin(User user) {
        return user.getRoles().contains(Role.ADMIN);
    }
}
