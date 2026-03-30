package com.example.mlbf.domain.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "Users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(max = 50, message = "아이디는 50자 이하여야 합니다.")
    @Column(name = "user_id", length = 50, nullable = false, unique = true)
    private String userId;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(max = 255, message = "비밀번호는 255자 이하여야 합니다.")
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Size(max = 20, message = "이메일은 20자 이하여야 합니다.")
    @Email(message = "올바른 이메일을 입력해야 합니다.")
    @Column(name = "email", length = 50, nullable = false, unique = true)
    private String email;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
    @Column(name = "nickname", length = 50, nullable = false)
    private String nickname;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public UserEntity(String userId, String password, String email, String nickname, UserRole role) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
    }

    // 비밀번호 수정
    public UserEntity updatePassword(String password) {
        this.password = password;
        return this;
    }

    // 닉네임 수정 (우리 프로젝트는 password + nickname만 수정 가능)
    public UserEntity updateNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    // DTO로부터 엔티티 생성
    public static UserEntity from(@NotNull UserDto userDto) {
        return UserEntity.builder()
                .userId(userDto.getUserId())
                .password(userDto.getPassword())
                .email(userDto.getEmail())
                .nickname(userDto.getNickname())
                .build();
    }

    // DTO로 전체 업데이트
    public UserEntity update(@NotNull UserDto userDto) {
        this.password = userDto.getPassword();
        this.nickname = userDto.getNickname();
        return this;
    }
}
