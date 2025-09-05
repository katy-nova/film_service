package com.example.films.controller;

import com.example.films.dto.user.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.films.dto.FriendshipDto;
import com.example.films.dto.user.*;
import com.example.films.service.AuthenticationService;
import com.example.films.service.FriendshipService;
import com.example.films.service.UserService;

@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
@Slf4j
@Tag(name = "Пользователи",
        description = "Все операции, кроме регистрации, доступны только авторизованным пользователям")
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final FriendshipService friendshipService;

    @Operation(summary = "Получения списка всех пользователей",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public Page<UserDto> getUsers(@PageableDefault Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    //если данные попытается получить заблокированный пользователь - будет ошибка
    @Operation(summary = "Получения данных пользователя по его id",
            description = "Если данные попытается получить пользователь из 'черного списка' - доступ будет запрещен",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(path = "/{id}")
    @PreAuthorize("@authenticationService.isBlockedByCurrentUser(#id)")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @Operation(summary = "регистрация пользователей")
    @Tag(name = "Действия с учетной записью")
    @PostMapping(path = "/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public NewUserDto createUser(@Valid @RequestBody UserCreateDto user) {
        return userService.createUser(user);
    }

    @Operation(summary = "Изменение данных пользователя",
            description = "Можно изменить только свои данные. Изменение чужих данных доступно только для администратора",
            security = @SecurityRequirement(name = "bearerAuth"))
    @Tag(name = "Действия с учетной записью")
    @PutMapping(path = "/{id}")
    @PreAuthorize("@authenticationService.isCurrentUserOrAdmin(#id)")
    public UserDto updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto user) {
        return userService.updateUser(id, user);
    }

    @Operation(summary = "Удаление пользователя",
            description = "Удалить можно только свой профиль. Удаление чужих данных доступно только для администратора",
            security = @SecurityRequirement(name = "bearerAuth"))
    @Tag(name = "Действия с учетной записью")
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@authenticationService.isCurrentUserOrAdmin(#id)")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @Operation(summary = "Отправить заявку в друзья",
            description = "Действие можно совершить только от своего имени",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(path = "/{userId}/friends/{friendId}")
    @PreAuthorize("@authenticationService.isCurrentUser(#userId)")
    public FriendshipDto addFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        return friendshipService.sendRequest(userId, friendId);
    }

    @Operation(summary = "Принять заявку в друзья",
            description = "Действие можно совершить только от своего имени",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(path = "/{userId}/friends/{friendId}/accept")
    @PreAuthorize("@authenticationService.isCurrentUser(#userId)")
    public FriendshipDto acceptFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        return friendshipService.sendRequest(userId, friendId);
    }

    @Operation(summary = "Добавить другого пользователя в 'черный список'",
            description = "Действие можно совершить только от своего имени",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(path = "/{userId}/friends/{friendId}/block")
    @PreAuthorize("@authenticationService.isCurrentUser(#userId)")
    public FriendshipDto blockFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        return friendshipService.addToBlackList(userId, friendId);
    }

    @Operation(summary = "Убрать другого пользователя в 'черный список'",
            description = "Действие можно совершить только от своего имени",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping(path = "/{userId}/friends/{friendId}/block")
    @PreAuthorize("@authenticationService.isCurrentUser(#userId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblockFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        friendshipService.removeFromBlackList(userId, friendId);
    }

    @Operation(summary = "Убрать другого пользователя из друзей",
            description = "Действие можно совершить только от своего имени",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping(path = "/{userId}/friends/{friendId}")
    @PreAuthorize("@authenticationService.isCurrentUser(#userId)")
    public FriendshipDto deleteFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        return friendshipService.removeFromFriends(userId, friendId);
    }

    @Operation(summary = "Получение списка друзей пользователя",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(path = "/{id}/friends")
    public Page<UserSimpleDto> getFriends(@PathVariable Long id, Pageable pageable) {
        return friendshipService.getFriendsByUserId(id, pageable);
    }

//    @GetMapping(path = "/{id}/friends/list")
//    public List<UserSimpleDto> getFriends(@PathVariable Long id) {
//        return friendshipService.getFriendsByUserId(id);
//    }

    @Operation(summary = "Получение списка подписчиков пользователя",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(path = "/{id}/followers")
    public Page<UserSimpleDto> getFollowers(@PathVariable Long id, @PageableDefault Pageable pageable) {
        return friendshipService.getFollowersByUserId(id, pageable);
    }

    @Operation(summary = "Получение списка подписок пользователя",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(path = "/{id}/following")
    public Page<UserSimpleDto> getFollowing(@PathVariable Long id, @PageableDefault Pageable pageable) {
        return friendshipService.getFollowingByUserId(id, pageable);
    }

    @Operation(summary = "Получение 'черного списка' пользователя",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(path = "/{id}/blacklist")
    public Page<UserSimpleDto> getBlacklist(@PathVariable Long id, @PageableDefault Pageable pageable) {
        return friendshipService.getBlackListByUserId(id, pageable);
    }

    @Operation(summary = "Получение списка общих друзей двух пользователей",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(path = "/{id}/friends/common/{otherId}")
    public Page<UserSimpleDto> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId,
                                                @PageableDefault Pageable pageable) {
        return friendshipService.getCommonFriends(id, otherId, pageable);
    }

    @Operation(summary = "Выдать пользователю права администратора",
            description = "Данное действие доступно только для администраторов",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(path = "/admin/make/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") // вообще этот путь я указывала в конфигураторе, как только для админов
    public ResponseEntity<String> makeAdmin(@PathVariable Long id) {
        authenticationService.makeAdmin(id);
        String message = String.format("Для пользователя с id: %s установлены права администратора", id);
        log.info(message);
        return ResponseEntity.ok(message);
    }

    @Operation(summary = "Аннулировать пользователю права администратора",
            description = "Данное действие доступно только для администраторов",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(path = "/admin/remove/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> removeAdmin(@PathVariable Long id) {
        authenticationService.makeUser(id);
        String message = String.format("Для пользователя с id: %s аннулированы права администратора", id);
        log.info(message);
        return ResponseEntity.ok(message);
    }

    @Operation(summary = "Заблокировать пользователя",
            description = "Данное действие доступно только для администраторов",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(path = "/admin/enable/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> enableUser(@PathVariable Long id) {
        authenticationService.makeEnabled(id);
        String message = String.format("Пользователь с id: %s активирован", id);
        log.info(message);
        return ResponseEntity.ok(message);
    }

    @Operation(summary = "Разблокировать пользователя",
            description = "Данное действие доступно только для администраторов",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(path = "/admin/disable/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> disableUser(@PathVariable Long id) {
        authenticationService.makeDisabled(id);
        String message = String.format("Пользователь с id: %s деактивирован", id);
        log.info(message);
        return ResponseEntity.ok(message);
    }
}
