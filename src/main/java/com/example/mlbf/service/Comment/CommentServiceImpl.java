package com.example.mlbf.service.Comment;

import com.example.mlbf.domain.Comment.CommentDto;
import com.example.mlbf.domain.Comment.CommentEntity;
import com.example.mlbf.domain.Post.PostEntity;
import com.example.mlbf.domain.User.UserEntity;
import com.example.mlbf.repository.Post.PostRepository;
import com.example.mlbf.repository.Comment.CommentRepository;
import com.example.mlbf.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentServiceImpl implements CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    // 댓글 작성
    @Override
    public CommentDto create(Long postId, Long userId, CommentDto commentDto) {
        log.info("CREATE: postId = {}, userId = {}, commentDto = {}", postId, userId, commentDto);

        // 게시물 조회
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시물입니다."));

        // 작성자 조회
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 댓글 저장
        CommentEntity savedEntity = commentRepository.save(
                CommentEntity.from(commentDto, userEntity, postEntity)
        );
        log.info("CREATE: savedEntity = {}", savedEntity);

        return CommentDto.from(savedEntity);
    }

    // 댓글 목록 조회
    @Override
    @Transactional
    public List<CommentDto> getList(Long postId) {
        log.info("getList: postId = {}", postId);

        // 게시물 존재 여부 확인
        postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 게시물입니다."));

        return commentRepository.findByPostId(postId).stream()
                .map(CommentDto::from)
                .collect(Collectors.toList());
    }

    // 댓글 수정 (본인만 가능)
    @Override
    public boolean update(Long commentId, Long userId, CommentDto commentDto) {
        log.info("UPDATE: commentId = {}, userId = {}, commentDto = {}", commentId, userId, commentDto);

        return commentRepository.findById(commentId).map(commentEntity -> {
            // 본인 확인
            if (!commentEntity.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("수정 권한이 없습니다.");
            }
            commentEntity.updateContent(commentDto.getContent());
            commentRepository.save(commentEntity);
            return true;
        }).orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 댓글입니다."));
    }

    // 댓글 삭제 (본인만 가능)
    @Override
    public boolean delete(Long commentId, Long userId) {
        log.info("DELETE: commentId = {}, userId = {}", commentId, userId);

        return commentRepository.findById(commentId).map(commentEntity -> {
            // 본인 확인
            if (!commentEntity.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("삭제 권한이 없습니다.");
            }
            commentRepository.delete(commentEntity);
            return true;
        }).orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 댓글입니다."));
    }

    // 내가 쓴 댓글 목록 조회 (마이페이지)
    @Override
    public List<CommentDto> getListByUser(Long userId) {
        log.info("getListByUser: userId = {}", userId);

        return commentRepository.findByUserId(userId).stream()
                .map(CommentDto::from)
                .collect(Collectors.toList());
    }
}
