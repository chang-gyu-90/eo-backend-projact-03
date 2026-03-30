package com.example.mlbf.service.User;

import com.example.mlbf.domain.User.*;
import java.util.Optional;

public interface UserService {

    // 회원가입
    void signup(UserDto userDto);

    // 사용자 정보 조회 (id로)
    Optional<UserDto> read(Long id);

    // 사용자 정보 조회 (userId로)
    Optional<UserDto> read(String userId);

    // 사용자 정보 수정 (password, nickname)
    Optional<UserDto> update(UserDto userDto);

    boolean existsByUserId(String userId);

    // 회원 탈퇴 (비밀번호 확인 후)
    boolean delete(Long id, String password);

    // 로그인 -> JWT 발급
    JwtResponseDto login(LoginDto loginDto);

    // 로그아웃 -> Refresh Token 무효화
    void logout(String refreshToken);
}
