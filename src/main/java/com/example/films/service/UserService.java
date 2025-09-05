package com.example.films.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.films.dto.mapping.UserMapping;
import com.example.films.dto.user.NewUserDto;
import com.example.films.dto.user.UserCreateDto;
import com.example.films.dto.user.UserDto;
import com.example.films.dto.user.UserUpdateDto;
import com.example.films.exception.AlreadyExistsException;
import com.example.films.exception.NotFoundException;
import com.example.films.model.entity.User;
import com.example.films.model.enums.Role;
import com.example.films.repository.ReviewRepository;
import com.example.films.repository.UserRepository;

import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final UserMapping userMapping;
    private final PasswordEncoder passwordEncoder;

    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapping::toDto);
    }

    @CachePut(cacheNames = "longUserCache", key = "#result.id")
    @Transactional
    public NewUserDto createUser(UserCreateDto userDto) {
        User user = userMapping.fromCreateDto(userDto);
        checkEmail(user.getEmail());
        checkLogin(user.getLogin());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // по умолчанию всех регаем как юзеров, только другой админ может выдать права админа
        user.setRoles(Set.of(Role.USER));
        if (user.getName() == null) {
            user.setName(user.getLogin());
            log.info("Для пользователя без имени установлено имя в соответствии с логином: {}", user.getLogin());
        }
        log.info("Пользователь сохранен");
        userRepository.save(user);
        return userMapping.toNewUserDto(user);
    }

    public UserDto getUser(String email) {
        return userRepository.findByEmail(email).map(userMapping::toDto)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Пользователь с email '%s' не найден", email);
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                });
    }

    public UserDto getUser(Long id) {
        return userMapping.toDto(findById(id));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Caching(evict = {@CacheEvict(cacheNames = "longUserCache", key = "#id"),
            @CacheEvict(cacheNames = "shortUserCache")})
    public void deleteUser(Long id) {
        log.debug("Попытка удалить пользователя с ID: {}", id);
        User user = findById(id);
        log.debug("Попытка удалить все отзывы пользователя с ID: {}", id);
        reviewRepository.deleteAllByUserId(id);
        log.info("Отзывы успешно удалены");
        userRepository.delete(user);
        log.info("Пользователь с ID: {} успешно удален", id);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CachePut(cacheNames = "longUserCache", key = "#id")
    public UserDto updateUser(Long id, UserUpdateDto user) {
        User oldUser = findById(id);
        if (!oldUser.getLogin().equals(user.getLogin()) && user.getLogin() != null) {
            log.debug("Попытка обновить логин");
            checkLogin(user.getLogin());
            oldUser.setLogin(user.getLogin());
            log.info("логин: {} успешно установлен", user.getLogin());
        }
        if (!oldUser.getEmail().equals(user.getEmail()) && user.getEmail() != null) {
            log.debug("Попытка обновить email");
            log.debug("Проверка повторного использования email");
            checkEmail(user.getEmail());
            oldUser.setEmail(user.getEmail());
            log.info("email: {} успешно установлен", user.getEmail());
        }
        if (!oldUser.getBirthday().equals(user.getBirthday()) && user.getBirthday() != null) {
            log.debug("Попытка обновить дату рождения");
            oldUser.setBirthday(user.getBirthday());
            log.info("дата рождения: {} успешно установлена", user.getBirthday());
        }
        if (!oldUser.getName().equals(user.getName()) && user.getName() != null) {
            log.debug("Попытка обновить имя пользователя");
            oldUser.setName(user.getName());
            log.info("имя пользователя: {} успешно установлено", user.getName());
        }
        if (!oldUser.getPassword().equals(user.getPassword()) && user.getPassword() != null) {
            log.debug("Попытка обновить пароль пользователя");
            oldUser.setPassword(user.getPassword());
            log.info("пароль пользователя: {} успешно установлен", user.getPassword());
        }
        userRepository.save(oldUser);
        log.info("Пользователь успешно сохранен");
        return userMapping.toDto(oldUser);
    }

    private void checkEmail(String email) {
        Optional<User> maybeUser = userRepository.findByEmail(email);
        if (maybeUser.isPresent()) {
            String errorMessage = String.format("Пользователь с email '%s' уже существует", email);
            log.error(errorMessage);
            throw new AlreadyExistsException(errorMessage);
        }
    }

    private void checkLogin(String login) {
        Optional<User> maybeUser = userRepository.findByLogin(login);
        if (maybeUser.isPresent()) {
            String errorMessage = String.format("Пользователь с логином '%s' уже существует", login);
            log.error(errorMessage);
            throw new AlreadyExistsException(errorMessage);
        }
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Пользователь с id '%s' не найден", id);
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                });
    }

}
