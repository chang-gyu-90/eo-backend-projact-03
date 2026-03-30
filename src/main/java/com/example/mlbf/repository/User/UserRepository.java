package com.example.mlbf.repository.User;

import com.example.mlbf.domain.User.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // 아이디 중복 체크 (회원가입)
    boolean existsByUserId(String userId);

    // 이메일 중복 체크 (회원가입)
    boolean existsByEmail(String email);

    // 닉네임 중복 체크 (정보 수정)
    boolean existsByNickname(String nickname);

    // 로그인 시 아이디로 조회
    Optional<UserEntity> findByUserId(String userId);

    // 이메일로 조회
    Optional<UserEntity> findByEmail(String email);

    // 닉네임으로 사용자 조회
    Optional<UserEntity> findByNickname(String nickname);
}
