package com.example.mlbf.service.User;

import com.example.mlbf.domain.User.*;
import com.example.mlbf.repository.Comment.CommentRepository;
import com.example.mlbf.repository.Post.PostRepository;
import com.example.mlbf.repository.User.UserRepository;
import com.example.mlbf.security.JwtProvider;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // 회원가입
    @Override
    public void signup(@NotNull UserDto userDto) {
        // 아이디 중복 체크
        if (userRepository.existsByUserId(userDto.getUserId())) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }
        // 이메일 중복 체크
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        // 비밀번호 암호화
        setEncodedPassword(userDto);

        // 저장
        UserEntity savedEntity = userRepository.save(UserEntity.from(userDto));
        userDto.setId(savedEntity.getId());
    }

    @Override
    public boolean existsByUserId(String userId) {
        return userRepository.existsByUserId(userId);
    }

    // 사용자 정보 조회 (id로)
    @Override
    public Optional<UserDto> read(@NotNull Long id) {
        return userRepository.findById(id).map(UserDto::from);
    }

    // 사용자 정보 조회 (userId로)
    @Override
    public Optional<UserDto> read(@NotNull String userId) {
        return userRepository.findByUserId(userId).map(UserDto::from);
    }

    // 사용자 정보 수정 (password, nickname)
    @Override
    public Optional<UserDto> update(@NotNull UserDto userDto) {
        // 닉네임 중복 체크 - 본인 닉네임은 제외
        userRepository.findByNickname(userDto.getNickname()).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(userDto.getId())) {
                throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
            }
        });

        // 비밀번호 암호화
        setEncodedPassword(userDto);

        return userRepository.findById(userDto.getId()).map(userEntity ->
                UserDto.from(userRepository.save(userEntity.update(userDto)))
        );
    }

    // 회원 탈퇴 (비밀번호 확인 후 삭제)
    @Override
    @Transactional
    public boolean delete(@NotNull Long id, @NotNull String password) {
        return userRepository.findById(id).map(userEntity -> {
            // 비밀번호 일치 여부 확인
            if (!passwordEncoder.matches(password, userEntity.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
            }

            // 댓글 먼저 삭제
            commentRepository.deleteByUserId(userEntity.getId());

            // 게시물 먼저 삭제
            postRepository.deleteByUserId(userEntity.getId());

            // 유저 삭제
            userRepository.delete(userEntity);
            return true;
        }).orElse(false);
    }

    // 로그인 -> JWT 발급
    @Override
    public JwtResponseDto login(@NotNull LoginDto loginDto) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(loginDto.getUserId(), loginDto.getPassword());

        try {
            Authentication authentication = authenticationManager.authenticate(token);
            return jwtProvider.getJwtResponseDto(authentication);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    // 로그아웃 -> Refresh Token 무효화
    @Override
    public void logout(@NotNull String refreshToken) {
        jwtProvider.invalidateRefreshToken(refreshToken);
    }

    // 비밀번호 암호화
    private UserDto setEncodedPassword(@NotNull UserDto userDto) {
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        userDto.setPassword(encodedPassword);
        return userDto;
    }
}
