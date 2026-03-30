package com.example.mlbf.domain.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginDto {

    @NotBlank(message = "아이디, 비밀번호를 반드시 입력해야 합니다.")
    @Size(max = 50, message = "아이디는 50자 이하여야 합니다.")
    private String userId;

    @NotBlank(message = "아이디, 비밀번호를 반드시 입력해야 합니다.")
    private String password;
}
