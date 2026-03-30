package com.example.mlbf.security;

import com.example.mlbf.domain.User.UserDto;
import com.example.mlbf.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@NullMarked
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // UserDto.from()은 password 제외하므로 엔티티에서 직접 변환 시 주의
        UserDto userDto = userRepository.findByUserId(userId)
                .map(userEntity -> UserDto.builder()
                        .id(userEntity.getId())
                        .userId(userEntity.getUserId())
                        .password(userEntity.getPassword()) // 인증을 위해 password 포함
                        .email(userEntity.getEmail())
                        .nickname(userEntity.getNickname())
                        .createdAt(userEntity.getCreatedAt())
                        .updatedAt(userEntity.getUpdatedAt())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(userId));

        return CustomUserDetails.of(userDto);
    }
}