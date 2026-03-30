package com.example.mlbf.repository.Post;


import com.example.mlbf.domain.Board.BoardEntity;
import com.example.mlbf.domain.Post.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    void deleteByUserId(Long userId);

    // 게시판 별 게시물 목록 조회 (페이징)
    Page<PostEntity> findByBoard(BoardEntity board, Pageable pageable);

    // 작성자 기준 게시물 조회
    Page<PostEntity> findByUserId(Long userId, Pageable pageable);
}
