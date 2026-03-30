package com.example.mlbf.ServiceTest;

import com.example.mlbf.domain.User.UserDto;
import com.example.mlbf.service.User.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class UserServiceTest {

    @Autowired
    private UserService userService;

    private Long testUserId;

    @BeforeEach
    public void setUp() {
        UserDto userDto = UserDto.builder()
                .userId("testuser")
                .password("test1234")
                .email("test@test.com")
                .nickname("테스터")
                .build();
        userService.signup(userDto);
        testUserId = userDto.getId();

        log.info("setUp: testUserId = {}", testUserId);
    }

    @Test
    public void testExists() {
        assertNotNull(userService);
        log.info("userService = {}", userService);
    }

    @Test
    public void testSignup() {
        UserDto userDto = UserDto.builder()
                .userId("testuser2")
                .password("test1234")
                .email("test2@test.com")
                .nickname("테스터2")
                .build();

        userService.signup(userDto);
        assertNotNull(userDto.getId());
        log.info("userDto = {}", userDto);
    }

    @Test
    public void testSignupDuplicateUserId() {
        // 중복 아이디로 가입 시도
        UserDto userDto = UserDto.builder()
                .userId("testuser")  // 이미 존재하는 아이디
                .password("test1234")
                .email("other@test.com")
                .nickname("다른사람")
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            userService.signup(userDto);
        });
        log.info("testSignupDuplicateUserId 통과");
    }

    @Test
    public void testSignupDuplicateEmail() {
        // 중복 이메일로 가입 시도
        UserDto userDto = UserDto.builder()
                .userId("otheruser")
                .password("test1234")
                .email("test@test.com")  // 이미 존재하는 이메일
                .nickname("다른사람")
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            userService.signup(userDto);
        });
        log.info("testSignupDuplicateEmail 통과");
    }

    @Test
    public void testReadById() {
        userService.read(testUserId).ifPresent(userDto -> {
            assertNotNull(userDto);
            log.info("userDto = {}", userDto);
        });
    }

    @Test
    public void testReadByUserId() {
        userService.read("testuser").ifPresent(userDto -> {
            assertNotNull(userDto);
            log.info("userDto = {}", userDto);
        });
    }

    @Test
    public void testUpdate() {
        UserDto userDto = UserDto.builder()
                .id(testUserId)
                .password("newpassword")
                .nickname("수정된닉네임")
                .build();

        userService.update(userDto).ifPresent(updatedDto -> {
            assertNotNull(updatedDto);
            log.info("updatedDto = {}", updatedDto);
        });
    }

    @Test
    public void testUpdateDuplicateNickname() {
        // 중복 닉네임으로 수정 시도
        UserDto userDto = UserDto.builder()
                .id(testUserId)
                .password("newpassword")
                .nickname("테스터")  // 이미 존재하는 닉네임
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            userService.update(userDto);
        });
        log.info("testUpdateDuplicateNickname 통과");
    }

    @Test
    public void testDelete() {
        assertTrue(userService.delete(testUserId, "test1234"));
        log.info("testDelete 통과");
    }

    @Test
    public void testDeleteWrongPassword() {
        // 잘못된 비밀번호로 탈퇴 시도
        assertThrows(IllegalArgumentException.class, () -> {
            userService.delete(testUserId, "wrongpassword");
        });
        log.info("testDeleteWrongPassword 통과");
    }
}
