package com.example.mlbf.ServiceTest;

import com.example.mlbf.domain.Board.BoardDto;
import com.example.mlbf.domain.Comment.CommentDto;
import com.example.mlbf.domain.Post.PostDto;
import com.example.mlbf.domain.User.UserDto;
import com.example.mlbf.service.Board.BoardService;
import com.example.mlbf.service.Comment.CommentService;
import com.example.mlbf.service.Post.PostService;
import com.example.mlbf.service.User.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private BoardService boardService;

    private Long testUserId;
    private Long testBoardId;
    private Long testPostId;
    private Long testCommentId;

    @BeforeEach
    public void setUp() {
        // 사용자 생성
        UserDto userDto = UserDto.builder()
                .userId("testuser")
                .password("test1234")
                .email("test@test.com")
                .nickname("테스터")
                .build();
        userService.signup(userDto);
        testUserId = userDto.getId();

        // 게시판 생성
        BoardDto boardDto = BoardDto.builder()
                .name("테스트 게시판")
                .build();
        boardService.createBoard(boardDto);
        testBoardId = boardDto.getId();

        // 게시물 생성
        PostDto postDto = PostDto.builder()
                .boardId(testBoardId)
                .category("일반")
                .title("[TEST] CommentServiceTest")
                .content("[TEST] CommentServiceTest")
                .build();
        postService.create(postDto, testUserId);
        testPostId = postDto.getId();

        log.info("setUp: testUserId = {}, testBoardId = {}, testPostId = {}",
                testUserId, testBoardId, testPostId);
    }

    @Test
    public void testExists() {
        assertNotNull(commentService);
        log.info("commentService = {}", commentService);
    }

    @Test
    public void testCreate() {
        CommentDto commentDto = CommentDto.builder()
                .content("[TEST] CommentServiceTest#testCreate")
                .build();

        log.info("commentDto = {}", commentDto);
        CommentDto savedDto = commentService.create(testPostId, testUserId, commentDto);
        log.info("savedDto = {}", savedDto);

        assertNotNull(savedDto);
        assertNotNull(savedDto.getId());
    }

    @Test
    public void testGetList() {
        // 댓글 먼저 생성
        CommentDto commentDto = CommentDto.builder()
                .content("[TEST] CommentServiceTest#testGetList")
                .build();
        commentService.create(testPostId, testUserId, commentDto);

        List<CommentDto> commentDtoList = commentService.getList(testPostId);
        assertNotNull(commentDtoList);
        assertFalse(commentDtoList.isEmpty());
        log.info("commentDtoList.size() = {}", commentDtoList.size());
    }

    @Test
    public void testUpdate() {
        // 댓글 먼저 생성
        CommentDto commentDto = CommentDto.builder()
                .content("[TEST] CommentServiceTest#testUpdate")
                .build();
        CommentDto savedDto = commentService.create(testPostId, testUserId, commentDto);
        testCommentId = savedDto.getId();

        // 댓글 수정
        CommentDto updateDto = CommentDto.builder()
                .content("[TEST] CommentServiceTest#testUpdate - updated")
                .build();

        assertTrue(commentService.update(testCommentId, testUserId, updateDto));
        log.info("testUpdate 통과");
    }

    @Test
    public void testUpdateNoPermission() {
        // 댓글 먼저 생성
        CommentDto commentDto = CommentDto.builder()
                .content("[TEST] CommentServiceTest#testUpdateNoPermission")
                .build();
        CommentDto savedDto = commentService.create(testPostId, testUserId, commentDto);
        testCommentId = savedDto.getId();

        // 다른 사용자로 수정 시도
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.update(testCommentId, 999L, CommentDto.builder()
                    .content("수정 시도")
                    .build());
        });
        log.info("testUpdateNoPermission 통과");
    }

    @Test
    public void testDelete() {
        // 댓글 먼저 생성
        CommentDto commentDto = CommentDto.builder()
                .content("[TEST] CommentServiceTest#testDelete")
                .build();
        CommentDto savedDto = commentService.create(testPostId, testUserId, commentDto);
        testCommentId = savedDto.getId();

        assertTrue(commentService.delete(testCommentId, testUserId));
        log.info("testDelete 통과");
    }

    @Test
    public void testDeleteNoPermission() {
        // 댓글 먼저 생성
        CommentDto commentDto = CommentDto.builder()
                .content("[TEST] CommentServiceTest#testDeleteNoPermission")
                .build();
        CommentDto savedDto = commentService.create(testPostId, testUserId, commentDto);
        testCommentId = savedDto.getId();

        // 다른 사용자로 삭제 시도
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.delete(testCommentId, 999L);
        });
        log.info("testDeleteNoPermission 통과");
    }

    @Test
    public void testGetListByUser() {
        // 댓글 먼저 생성
        CommentDto commentDto = CommentDto.builder()
                .content("[TEST] CommentServiceTest#testGetListByUser")
                .build();
        commentService.create(testPostId, testUserId, commentDto);

        List<CommentDto> commentDtoList = commentService.getListByUser(testUserId);
        assertNotNull(commentDtoList);
        assertFalse(commentDtoList.isEmpty());
        log.info("commentDtoList.size() = {}", commentDtoList.size());
    }
}
