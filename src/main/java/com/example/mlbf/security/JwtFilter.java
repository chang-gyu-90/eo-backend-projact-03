package com.example.mlbf.security;

import com.example.mlbf.domain.User.UserDto;
import com.example.mlbf.domain.User.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 비회원 접근 가능 경로는 토큰 검증 없이 통과
        String path = request.getRequestURI();

        if (path.startsWith("/boards") ||
                path.startsWith("/board/list") ||
                path.startsWith("/board/read") ||
                path.equals("/account/signup") ||   // ← signup만
                path.equals("/account/login") ||    // ← login만
                path.matches(".*/board/\\d+/comment/list")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더에서 JWT 추출
        String header = request.getHeader("Authorization");

        // Authorization 헤더가 없거나 "Bearer "로 시작하지 않으면 인증 중단
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }


        // "Bearer " 이후 JWT 추출
        String token = header.replace("Bearer ", "");

        try {
            // JWT 만료 여부 확인
            if (jwtProvider.isExpired(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 만료되었습니다.");
                return;
            }

            // 블랙리스트 토큰 확인 (로그아웃 처리된 토큰)
            if (jwtProvider.isBlacklisted(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그아웃된 토큰입니다.");
                return;
            }

            // JWT에서 사용자 정보 추출
            String userId = jwtProvider.getUserId(token);

            // Authentication 인스턴스 생성
            UserDto userDto = UserDto.builder()
                    .userId(userId)
                    .password("N/A")
                    .role(UserRole.USER)
                    .build();

            UserDetails userDetails = CustomUserDetails.of(userDto);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            // SecurityContext에 인증 정보 저장
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
