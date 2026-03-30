package com.example.mlbf.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtProvider jwtProvider;
    private final AuthenticationConfiguration authenticationConfiguration;

    /**
     * AuthenticationManager 빈 등록
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Security 필터 제외 경로 설정
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return webSecurity -> webSecurity.ignoring()
                .requestMatchers("/h2-console/**")
                .requestMatchers("/static/**")
                .requestMatchers("/")
                .requestMatchers("/index.html")
                .requestMatchers("/html/**")
                .requestMatchers("/css/**")
                .requestMatchers("/js/**")
                .requestMatchers("/images/**")
                .requestMatchers("/favicon.ico")
                .requestMatchers("/board/*/comment/list");
    }

    /**
     * SecurityFilterChain 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // REST API라 불필요한 설정 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 사용 안 함 (JWT 사용)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 인가 설정
                .authorizeHttpRequests(authorize -> authorize

                        // 인증 없이 접근 가능한 URL
                        .requestMatchers(
                                "/account/signup",
                                "/account/login",
                                "/account/check-userid"
                        ).permitAll()

                        // 비회원 조회 가능한 URL
                        .requestMatchers(
                                "/boards",
                                "/board/list",
                                "/board/read",
                                "/board/*/comment/list"
                        ).permitAll()

                        // 나머지는 로그인 필요
                        .anyRequest().authenticated()
                )

                // JWT 필터 등록
                .addFilterBefore(
                        new JwtFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return httpSecurity.build();
    }

    /**
     * 비밀번호 암호화 빈 등록
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration corsConfiguration = new CorsConfiguration();

            corsConfiguration.setAllowedOrigins(List.of(
                    "http://localhost:3000",
                    "http://localhost:8080",
                    "http://localhost:5173",
                    "http://127.0.0.1:5500"  // VSCode Live Server
            ));

            // 모든 HTTP 메서드 허용
            corsConfiguration.setAllowedMethods(Collections.singletonList("*"));

            // 인증 정보 포함 허용
            corsConfiguration.setAllowCredentials(true);

            // 모든 헤더 허용
            corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));

            // Authorization 헤더 노출
            corsConfiguration.setExposedHeaders(Collections.singletonList("Authorization"));

            // 1시간 캐싱
            corsConfiguration.setMaxAge(3600L);

            return corsConfiguration;
        };
    }

}
