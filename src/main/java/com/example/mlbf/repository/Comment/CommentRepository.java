package com.example.mlbf.repository.Comment;

import com.example.mlbf.domain.Post.PostEntity;
import com.example.mlbf.domain.Comment.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    void deleteByUserId(Long userId);

    // 특정 게시물의 댓글 목록 조회 (PostEntity로)
    List<CommentEntity> findByPost(PostEntity post);

    // 특정 게시물의 댓글 목록 조회 (postId로)
    List<CommentEntity> findByPostId(Long postId);

    // 특정 게시물의 댓글 수 조회
    int countByPostId(Long postId);

    // 작성자 기준 댓글 목록 조회 (마이페이지)
    List<CommentEntity> findByUserId(Long userId);

    // 게시물 삭제 시 댓글 삭제
    void deleteByPostId(Long postId);
}
