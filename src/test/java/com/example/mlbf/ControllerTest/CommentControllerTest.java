package com.example.mlbf.ControllerTest;

import com.example.mlbf.domain.Board.BoardDto;
import com.example.mlbf.domain.Comment.CommentDto;
import com.example.mlbf.domain.Post.PostDto;
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
public class CommentControllerTest {

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
    private Long testPostId;

    @BeforeEach
    public void setUp() throws Exception {
        // 회원가입
        UserDto userDto = UserDto.builder()
                .userId(userId)
                .password(password)
                .email(email)
                .nickname(nickname)
                .build();
        performPost("/account/signup", userDto, null);

// 게시판 생성
        try {
            boardService.createBoard(BoardDto.builder()
                    .name("테스트 게시판")
                    .build());
        } catch (IllegalArgumentException e) {
            log.info("게시판 이미 존재: {}", e.getMessage());
        }

        Long testBoardId = boardService.getBoardList().stream()
                .filter(b -> b.getName().equals("테스트 게시판"))
                .findFirst()
                .map(BoardDto::getId)
                .orElseThrow(() -> new IllegalStateException("테스트 게시판을 찾을 수 없습니다."));


// 게시물 생성
        PostDto postDto = PostDto.builder()
                .boardId(testBoardId)  // ← 하드코딩 제거
                .category("일반")
                .title("[TEST] CommentControllerTest")
                .content("[TEST] CommentControllerTest")
                .build();

        String responseBody = performPost("/board/write", postDto, getAccessToken())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        testPostId = new JSONObject(responseBody).getLong("id");
    }

    @Test
    void testGetCommentList() throws Exception {
        performGet("/board/" + testPostId + "/comment/list", null)
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void testCreateComment() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .content("[TEST] CommentControllerTest#testCreateComment")
                .build();

        performPost("/board/" + testPostId + "/comment/write", commentDto, getAccessToken())
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    void testUpdateComment() throws Exception {
        // 댓글 먼저 생성
        CommentDto commentDto = CommentDto.builder()
                .content("[TEST] CommentControllerTest#testUpdateComment")
                .build();

        String commentResponse = performPost("/board/" + testPostId + "/comment/write", commentDto, getAccessToken())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long commentId = new JSONObject(commentResponse).getLong("id");

        // 댓글 수정
        CommentDto updateDto = CommentDto.builder()
                .id(commentId)
                .content("[TEST] CommentControllerTest#testUpdateComment - updated")
                .build();

        performPost("/board/" + testPostId + "/comment/update", updateDto, getAccessToken())
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void testDeleteComment() throws Exception {
        // 댓글 먼저 생성
        CommentDto commentDto = CommentDto.builder()
                .content("[TEST] CommentControllerTest#testDeleteComment")
                .build();

        String commentResponse = performPost("/board/" + testPostId + "/comment/write", commentDto, getAccessToken())
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long commentId = new JSONObject(commentResponse).getLong("id");

        // 댓글 삭제
        performPost("/board/" + testPostId + "/comment/delete", Map.of("comment_id", commentId), getAccessToken())
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
}
