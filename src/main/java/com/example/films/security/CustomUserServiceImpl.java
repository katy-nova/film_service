package com.example.films.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.films.model.entity.User;
import com.example.films.repository.UserRepository;


@Service
@RequiredArgsConstructor
public class CustomUserServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return new CustomUserDetails(user, user.getRoles());
    }
}
