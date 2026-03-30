package com.example.mlbf.RepositoryTest;

import com.example.mlbf.domain.Board.BoardEntity;
import com.example.mlbf.domain.Comment.CommentEntity;
import com.example.mlbf.domain.Post.PostEntity;
import com.example.mlbf.domain.User.UserEntity;
import com.example.mlbf.repository.Board.BoardRepository;
import com.example.mlbf.repository.Comment.CommentRepository;
import com.example.mlbf.repository.Post.PostRepository;
import com.example.mlbf.repository.User.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class UserRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    private UserEntity testUser;
    private PostEntity testPost;

    @BeforeEach
    public void setUp() {
        testUser = userRepository.save(UserEntity.builder()
                .userId("testuser")
                .password("test1234")
                .email("test@test.com")
                .nickname("테스터")
                .build());

        BoardEntity testBoard = boardRepository.save(BoardEntity.builder()
                .name("테스트 게시판")
                .build());

        testPost = postRepository.save(PostEntity.builder()
                .category("일반")
                .title("[TEST] CommentRepositoryTest")
                .content("[TEST] CommentRepositoryTest")
                .user(testUser)
                .board(testBoard)
                .build());

        commentRepository.save(CommentEntity.builder()
                .content("[TEST] CommentRepositoryTest")
                .user(testUser)
                .post(testPost)
                .build());

        log.info("setUp: testUser = {}, testPost = {}", testUser, testPost);
    }

    @Test
    public void testExists() {
        assertNotNull(commentRepository);
        log.info("commentRepository = {}", commentRepository);
    }

    @Test
    public void testSave() {
        CommentEntity commentEntity = CommentEntity.builder()
                .content("[TEST] CommentRepositoryTest#testSave")
                .user(testUser)
                .post(testPost)
                .build();
        CommentEntity savedEntity = commentRepository.save(commentEntity);
        assertNotNull(savedEntity.getId());
        log.info("savedEntity = {}", savedEntity);
    }

    @Test
    @Transactional
    public void testFindByPostId() {
        List<CommentEntity> commentEntityList = commentRepository.findByPostId(testPost.getId());
        assertNotNull(commentEntityList);
        assertFalse(commentEntityList.isEmpty());
        log.info("commentEntityList.size() = {}", commentEntityList.size());
    }

    @Test
    @Transactional
    public void testFindByUserId() {
        List<CommentEntity> commentEntityList = commentRepository.findByUserId(testUser.getId());
        assertNotNull(commentEntityList);
        assertFalse(commentEntityList.isEmpty());
        log.info("commentEntityList.size() = {}", commentEntityList.size());
    }

    @Test
    public void testCountByPostId() {
        int count = commentRepository.countByPostId(testPost.getId());
        assertTrue(count > 0);
        log.info("count = {}", count);
    }

    @Test
    public void testDelete() {
        CommentEntity commentEntity = commentRepository.findAll().get(0);
        commentRepository.delete(commentEntity);
        assertTrue(commentRepository.findAll().isEmpty());
        log.info("testDelete 통과");
    }

}
