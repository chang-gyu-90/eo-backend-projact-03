package com.example.mlbf.ServiceTest;

import com.example.mlbf.domain.Board.BoardDto;
import com.example.mlbf.domain.Post.PostDto;
import com.example.mlbf.domain.User.UserDto;
import com.example.mlbf.service.Board.BoardService;
import com.example.mlbf.service.Post.PostService;
import com.example.mlbf.service.User.UserService;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private BoardService boardService;

    private Long testUserId;
    private Long testBoardId;
    private Long testPostId;

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

        log.info("setUp: testUserId = {}, testBoardId = {}", testUserId, testBoardId);
    }

    @Test
    public void testExists() {
        assertNotNull(postService);
        log.info("postService = {}", postService);
    }

    @Test
    public void testCreate() {
        PostDto postDto = PostDto.builder()
                .boardId(testBoardId)
                .category("일반")
                .title("[TEST] PostServiceTest#testCreate")
                .content("[TEST] PostServiceTest#testCreate")
                .build();

        log.info("postDto = {}", postDto);
        postService.create(postDto, testUserId);
        log.info("postDto = {}", postDto);
        assertNotNull(postDto.getId());
    }

    @Test
    public void testRead() {
        // read 전에 게시물 먼저 생성
        PostDto postDto = PostDto.builder()
                .boardId(testBoardId)
                .category("일반")
                .title("[TEST] PostServiceTest#testRead")
                .content("[TEST] PostServiceTest#testRead")
                .build();
        postService.create(postDto, testUserId);
        testPostId = postDto.getId();

        PostDto readDto = postService.read(testPostId);
        assertNotNull(readDto);
        log.info("postDto = {}", readDto);
    }

    @Test
    public void testUpdate() {
        // update 전에 게시물 먼저 생성
        PostDto postDto = PostDto.builder()
                .boardId(testBoardId)
                .category("일반")
                .title("[TEST] PostServiceTest#testUpdate")
                .content("[TEST] PostServiceTest#testUpdate")
                .build();
        postService.create(postDto, testUserId);
        testPostId = postDto.getId();

        postDto.setTitle("[TEST] PostServiceTest#testUpdate - updated");
        postDto.setContent("[TEST] PostServiceTest#testUpdate - updated");

        assertTrue(postService.update(postDto, testUserId));
    }

    @Test
    public void testDelete() {
        assertThrows(IllegalArgumentException.class, () -> {
            postService.delete(999L, testUserId);
        });
    }

    @Test
    public void testGetList() {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                Sort.by(Sort.Direction.DESC, "id"));

        Page<PostDto> postDtoPage = postService.getList(testBoardId, pageable);

        assertThat(postDtoPage).isNotNull();
        assertThat(postDtoPage.getNumber()).isEqualTo(pageNumber);
        assertThat(postDtoPage.getSize()).isEqualTo(pageSize);
        log.info("postDtoPage.getTotalElements() = {}", postDtoPage.getTotalElements());
    }
}
