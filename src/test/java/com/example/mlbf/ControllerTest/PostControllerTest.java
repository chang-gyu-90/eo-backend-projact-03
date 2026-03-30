package com.example.mlbf.ControllerTest;

import com.example.mlbf.domain.Board.BoardDto;
import com.example.mlbf.domain.User.LoginDto;
import com.example.mlbf.domain.User.UserDto;
import com.example.mlbf.service.Board.BoardService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BoardService boardService;

    private final String userId = "testuser";
    private final String password = "test1234";
    private final String email = "test@test.com";
    private final String nickname = "테스터";

    public void setUp() throws Exception {
        // 회원가입 시도 (이미 있으면 무시)
        UserDto userDto = UserDto.builder()
                .userId(userId)
                .password(password)
                .email(email)
                .nickname(nickname)
                .build();
        performPost("/account/signup", userDto, null);

        // 게시판 생성 (이미 있으면 무시)
        try {
            boardService.createBoard(BoardDto.builder()
                    .name("테스트 게시판")
                    .build());
        } catch (IllegalArgumentException e) {
            log.info("게시판 이미 존재: {}", e.getMessage());
        }

        log.info("setUp: userId = {}", userId);
    }

    @Test
    void testUpdateUser() throws Exception {
        // 비밀번호 수정
        UserDto updateDto = UserDto.builder()
                .password("newpassword")
                .nickname("수정된닉네임")
                .build();

        performPost("/account/update", updateDto, getAccessToken())
                .andExpect(status().isOk())
                .andDo(print());

        // 원래 비밀번호로 복구
        String newTokenBody = performLogin(userId, "newpassword")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String newToken = new JSONObject(newTokenBody).getString("accessToken");

        UserDto revertDto = UserDto.builder()
                .password(password)
                .nickname(nickname)
                .build();

        performPost("/account/update", revertDto, newToken)
                .andExpect(status().isOk());

        log.info("testUpdateUser 통과");
    }

    @Test
    void testLogout() throws Exception {
        String refreshToken = getRefreshToken();
        performPost("/account/logout", Map.of("refresh_token", refreshToken), getAccessToken())
                .andExpect(status().isOk())
                .andDo(print());
    }

    private ResultActions performLogin(String userId, String password) throws Exception {
        LoginDto loginDto = LoginDto.builder()
                .userId(userId)
                .password(password)
                .build();
        return performPost("/account/login", loginDto, null);
    }

    private ResultActions performPost(String url, Object body, String accessToken) throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
        if (accessToken != null) {
            request.header("Authorization", "Bearer " + accessToken);
        }
        return mockMvc.perform(request);
    }

    private ResultActions performGet(String url, String accessToken) throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(url);
        if (accessToken != null) {
            request.header("Authorization", "Bearer " + accessToken);
        }
        return mockMvc.perform(request);
    }

    private String getAccessToken() throws Exception {
        String responseBody = performLogin(userId, password)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(responseBody);
        return jsonObject.getString("accessToken");
    }

    private String getRefreshToken() throws Exception {
        String responseBody = performLogin(userId, password)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(responseBody);
        return jsonObject.getString("refreshToken");
    }
}
