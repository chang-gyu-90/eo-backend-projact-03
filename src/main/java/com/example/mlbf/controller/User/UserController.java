package com.example.mlbf.controller.User;

import com.example.mlbf.domain.User.JwtResponseDto;
import com.example.mlbf.domain.User.LoginDto;
import com.example.mlbf.domain.User.UserDto;
import com.example.mlbf.service.User.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 회원가입
     * POST /account/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody UserDto userDto) {
        try {
            userService.signup(userDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "회원가입이 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 아이디 중복 체크
     * POST /account/check-userid
     */
    @PostMapping("/check-userid")
    public ResponseEntity<?> checkUserId(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        boolean available = !userService.existsByUserId(userId);
        return ResponseEntity.ok(Map.of("available", available));
    }

    /**
     * 로그인
     * POST /account/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto) {
        try {
            JwtResponseDto jwtResponseDto = userService.login(loginDto);
            return ResponseEntity.ok(jwtResponseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "아이디 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    /**
     * 로그아웃
     * POST /account/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refresh_token");
            userService.logout(refreshToken);
            return ResponseEntity.ok(Map.of("message", "로그아웃이 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인 이후 이용 바랍니다."));
        }
    }

    /**
     * 사용자 정보 조회
     * GET /account/info
     */
    @GetMapping("/info")
    public ResponseEntity<?> info(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return userService.read(userDetails.getUsername())
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인 이후 이용 바랍니다."));
        }
    }

    /**
     * 사용자 정보 수정
     * POST /account/update
     */
    @PostMapping("/update")
    public ResponseEntity<?> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserDto userDto) {
        try {
            // 현재 로그인한 사용자 조회 후 id 세팅
            return userService.read(userDetails.getUsername())
                    .map(currentUser -> {
                        userDto.setId(currentUser.getId());
                        return userService.update(userDto)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
                    })
                    .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 회원 탈퇴
     * POST /account/leave
     */
    @PostMapping("/leave")
    public ResponseEntity<?> leave(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            return userService.read(userDetails.getUsername())
                    .map(currentUser -> {
                        boolean deleted = userService.delete(currentUser.getId(), password);
                        if (deleted) {
                            return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다."));
                        }
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("message", "회원 탈퇴에 실패하였습니다."));
                    })
                    .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

}
