package com.example.mlbf.security;

import com.example.mlbf.domain.User.JwtResponseDto;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class JwtProvider {

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    // 로그아웃된 Refresh Token을 저장하는 블랙리스트
    // (추후 Redis로 교체 가능)
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;

        secretKey = new SecretKeySpec(
                jwtProperties.secretKey().getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );

        log.info("JWT secret-key: {}", jwtProperties.secretKey());
    }

    /**
     * JWT에서 userId 추출
     */
    public String getUserId(String token) {
        return getClaim(token, "userId");
    }

    /**
     * JWT에서 권한(authority) 추출
     */
    public String getAuthority(String token) {
        return getClaim(token, "authority");
    }

    /**
     * JWT에서 지정한 클레임(claim) 추출
     */
    public String getClaim(String token, String claim) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get(claim, String.class);
    }

    /**
     * JWT 만료 여부 확인
     */
    public boolean isExpired(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    /**
     * Refresh Token 블랙리스트 여부 확인
     */
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * Access 토큰 발급
     */
    public String issueAccessToken(String userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(getDateAfterDuration(jwtProperties.accessTokenExpiration()))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh 토큰 발급
     */
    public String issueRefreshToken(String userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(getDateAfterDuration(jwtProperties.refreshTokenExpiration()))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 무효화 (로그아웃)
     */
    public void invalidateRefreshToken(String refreshToken) {
        blacklistedTokens.add(refreshToken);
        log.info("Refresh Token 블랙리스트 등록 완료");
    }

    /**
     * 현재 시각에서 duration이 지난 시각 생성
     */
    private Date getDateAfterDuration(Duration duration) {
        return new Date(new Date().getTime() + duration.toMillis());
    }

    /**
     * Authentication 정보로 JwtResponseDto 생성
     */
    public JwtResponseDto getJwtResponseDto(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        assert userDetails != null;
        String userId = userDetails.getUsername(); // Spring Security는 getUsername() 사용


        String accessToken = issueAccessToken(userId);
        String refreshToken = issueRefreshToken(userId);

        return JwtResponseDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
