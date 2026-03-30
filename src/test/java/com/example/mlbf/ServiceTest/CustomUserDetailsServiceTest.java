package com.example.mlbf.ServiceTest;

import com.example.mlbf.domain.User.UserDto;
import com.example.mlbf.security.CustomUserDetailsService;
import com.example.mlbf.service.User.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setUp() {
        UserDto userDto = UserDto.builder()
                .userId("testuser")
                .password("test1234")
                .email("test@test.com")
                .nickname("테스터")
                .build();
        userService.signup(userDto);
        log.info("setUp: testUserId = {}", userDto.getId());
    }

    @Test
    public void testExists() {
        assertNotNull(customUserDetailsService);
        log.info("customUserDetailsService = {}", customUserDetailsService);
    }

    @Test
    public void testLoadUserByUsername() {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertNotNull(userDetails.getPassword());
        log.info("userDetails = {}", userDetails);
    }

    @Test
    public void testLoadUserByUsernameFail() {
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistent");
        });
        log.info("testLoadUserByUsernameFail 통과");
    }

    @Test
    public void testAuthorities() {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");
        assertNotNull(userDetails.getAuthorities());
        assertFalse(userDetails.getAuthorities().isEmpty());
        log.info("authorities = {}", userDetails.getAuthorities());
    }
}
