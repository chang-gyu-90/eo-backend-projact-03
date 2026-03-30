package com.example.mlbf.service.Post;

import com.example.mlbf.domain.Board.BoardEntity;
import com.example.mlbf.domain.Post.PostDto;
import com.example.mlbf.domain.Post.PostEntity;
import com.example.mlbf.domain.User.UserEntity;
import com.example.mlbf.repository.Board.BoardRepository;
import com.example.mlbf.repository.Post.PostRepository;
import com.example.mlbf.repository.Comment.CommentRepository;
import com.example.mlbf.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    // 게시물 작성
    @Override
    public void create(PostDto postDto, Long userId) {
        log.info("CREATE: postDto = {}, userId = {}", postDto, userId);

        // 작성자 조회
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 게시판 조회
        BoardEntity boardEntity = boardRepository.findById(postDto.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다."));

        // 게시물 저장
        PostEntity savedEntity = postRepository.save(
                PostEntity.from(postDto, userEntity, boardEntity)
        );
        log.info("CREATE: savedEntity = {}", savedEntity);
        postDto.setId(savedEntity.getId());
    }

    // 게시물 상세 조회 (조회수 증가)
    @Override
    @Transactional
    public PostDto read(Long id) {
        log.info("READ: id = {}", id);

        return postRepository.findById(id).map(postEntity -> {
            // 조회수 증가
            postEntity.increaseViews();
            postRepository.save(postEntity);

            // 댓글 수 조회 후 세팅
            PostDto postDto = PostDto.from(postEntity);
            postDto.setCommentCount(commentRepository.countByPostId(id));
            return postDto;
        }).orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시물입니다."));
    }

    // 게시물 수정 (본인만 가능)
    @Override
    public boolean update(PostDto postDto, Long userId) {
        log.info("UPDATE: postDto = {}, userId = {}", postDto, userId);

        return postRepository.findById(postDto.getId()).map(postEntity -> {
            // 본인 확인
            if (!postEntity.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("수정 권한이 없습니다.");
            }
            postEntity.update(postDto.getCategory(), postDto.getTitle(), postDto.getContent());
            postRepository.save(postEntity);
            return true;
        }).orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시물입니다."));
    }

    // 게시물 삭제 (본인만 가능)
    @Override
    public boolean delete(Long id, Long userId) {
        log.info("DELETE: id = {}, userId = {}", id, userId);

        return postRepository.findById(id).map(postEntity -> {
            // 본인 확인
            if (!postEntity.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("삭제 권한이 없습니다.");
            }
            postRepository.delete(postEntity);
            return true;
        }).orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시물입니다."));
    }

    // 게시물 목록 조회 (페이징, 게시판 필터)
    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getList(Long boardId, Pageable pageable) {
        log.info("getList: boardId = {}, pageable = {}", boardId, pageable);

        // boardId가 없으면 전체 조회
        if (boardId == null) {
            return postRepository.findAll(pageable).map(postEntity -> {
                PostDto postDto = PostDto.from(postEntity);
                postDto.setCommentCount(commentRepository.countByPostId(postEntity.getId()));
                return postDto;
            });
        }

        // boardId가 있으면 해당 게시판 게시물만 조회
        BoardEntity boardEntity = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시판입니다."));

        return postRepository.findByBoard(boardEntity, pageable).map(postEntity -> {
            PostDto postDto = PostDto.from(postEntity);
            postDto.setCommentCount(commentRepository.countByPostId(postEntity.getId()));
            return postDto;
        });
    }

    // 내가 쓴 게시물 목록 조회 (마이페이지)
    @Override
    public Page<PostDto> getListByUser(Long userId, Pageable pageable) {
        log.info("getListByUser: userId = {}", userId);

        return postRepository.findByUserId(userId, pageable).map(postEntity -> {
            PostDto postDto = PostDto.from(postEntity);
            postDto.setCommentCount(commentRepository.countByPostId(postEntity.getId()));
            return postDto;
        });
    }
}
