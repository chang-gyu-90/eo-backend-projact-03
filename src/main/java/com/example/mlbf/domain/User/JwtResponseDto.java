package com.example.mlbf.domain.User;

import lombok.*;

@Data
@AllArgsConstructor
@Builder
public class JwtResponseDto {

    // 토큰 타입 (Bearer)
    private String grantType;

    // 실제 API 요청 시 사용하는 토큰
    private String accessToken;

    // Access 토큰 만료 시 재발급용 토큰
    private String refreshToken;
}
