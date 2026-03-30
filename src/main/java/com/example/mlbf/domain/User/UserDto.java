package com.example.mlbf.domain.User;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private Long id;

    @NotBlank(message = "필수 입력 항목은 반드시 입력해야 합니다.")
    @Size(max = 50, message = "아이디는 50자 이하여야 합니다.")
    private String userId;

    @NotBlank(message = "필수 입력 항목은 반드시 입력해야 합니다.")
    @Size(max = 255, message = "비밀번호는 255자 이하여야 합니다.")
    private String password;

    @NotBlank(message = "필수 입력 항목은 반드시 입력해야 합니다.")
    @Size(max = 20, message = "이메일은 20자 이하여야 합니다.")
    @Email(message = "올바른 이메일을 입력해야 합니다.")
    private String email;

    @NotBlank(message = "필수 입력 항목은 반드시 입력해야 합니다.")
    @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
    private String nickname;

    @Builder.Default
    private UserRole role = UserRole.USER;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> Dto 변환 (비밀번호 제외 - 사용자 정보 조회 시 사용)
    public static UserDto from(@NotNull UserEntity userEntity) {
        return UserDto.builder()
                .id(userEntity.getId())
                .userId(userEntity.getUserId())
                .email(userEntity.getEmail())
                .nickname(userEntity.getNickname())
                .createdAt(userEntity.getCreatedAt())
                .updatedAt(userEntity.getUpdatedAt())
                .build();
    }

}
