package com.example.films.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.films.dto.jwt.JwtAuthenticationDto;
import com.example.films.dto.jwt.RefreshTokenDto;
import com.example.films.dto.jwt.UserCredentialDto;
import com.example.films.exception.AccessDenyException;
import com.example.films.exception.NotFoundException;
import com.example.films.exception.UnauthorizedException;
import com.example.films.model.entity.Friendship;
import com.example.films.model.entity.User;
import com.example.films.model.enums.FriendshipStatus;
import com.example.films.model.enums.Role;
import com.example.films.repository.FriendshipRepository;
import com.example.films.repository.UserRepository;
import com.example.films.security.CustomUserDetails;
import com.example.films.security.CustomUserServiceImpl;
import com.example.films.security.jwt.JwtService;

import javax.naming.AuthenticationException;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserServiceImpl customUserService;
    private final FriendshipRepository friendshipRepository;

    public JwtAuthenticationDto signIn(UserCredentialDto userCredentialDto) throws AuthenticationException {
        CustomUserDetails customUserDetails = findCustomUserDetailsByCredentials(userCredentialDto);
        return jwtService.generateAuthenticationToken(customUserDetails);

    }

    public JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws AuthenticationException {
        String refreshToken = refreshTokenDto.getRefreshToken();
        if (refreshToken != null && jwtService.validateJwtToken(refreshToken)) {
            String login = jwtService.getLoginFromToken(refreshToken);
            CustomUserDetails customUserDetails = customUserService.loadUserByUsername(login);
            return jwtService.refreshBaseToken(customUserDetails, refreshToken);

        }
        throw new AuthenticationException("Invalid refresh token");
    }

    private CustomUserDetails findCustomUserDetailsByCredentials(UserCredentialDto userCredentialDto)
            throws AuthenticationException {
        Optional<User> maybeUser = userRepository.findByLogin(userCredentialDto.getLogin());
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            if (passwordEncoder.matches(userCredentialDto.getPassword(), user.getPassword())) {
                return customUserService.loadUserByUsername(user.getLogin());
            }
        }
        throw new AuthenticationException("Неверные логин или пароль");
    }

    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException();
        }
        Long authenticatedUserId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDenyException("Вы пытаетесь совершить действие от чужого имени");
        }
        return true;
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException();
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals(Role.ADMIN.name()));
        if (!isAdmin) {
            throw new AccessDenyException("Для совершения данного действия необходимы права администратора");
        }
        return true;
    }

    public boolean isCurrentUserOrAdmin(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException();
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals(Role.ADMIN.name()));
        Long authenticatedUserId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        boolean isCurrentUser = (authenticatedUserId.equals(userId));
        if (!isCurrentUser && !isAdmin) {
            throw new AccessDenyException("Вы пытаетесь совершить действие от чужого имени или требующие права администратора");
        }
        return true;
    }

    // метод, который проверяет права доступа к данным пользователя
    public boolean isBlockedByCurrentUser(Long friendId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
        Optional<Friendship> maybeFriendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId);
        if (maybeFriendship.isPresent()) {
            Friendship friendship = maybeFriendship.get();
            if (friendship.getStatus().equals(FriendshipStatus.BLOCKED)
                    && friendship.getUser().getId().equals(friendId)) {
                throw new AccessDenyException("Пользователь ограничил доступ к своим данным");
            }
        }
        return true;
    }

    public void makeAdmin(Long userId) {
        User user = findUser(userId);
        user.makeAdmin();
    }

    public void makeUser(Long userId) {
        User user = findUser(userId);
        user.makeUser();
    }

    public void makeDisabled(Long userId) {
        User user = findUser(userId);
        user.setEnabled(false);
    }

    public void makeEnabled(Long userId) {
        User user = findUser(userId);
        user.setEnabled(true);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

}
