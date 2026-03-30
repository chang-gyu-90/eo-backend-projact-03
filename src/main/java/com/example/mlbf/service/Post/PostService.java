package com.example.mlbf.service.Post;

import com.example.mlbf.domain.Post.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {


    // 게시물 작성
    void create(PostDto postDto, Long userId);

    // 게시물 상세 조회 (조회수 증가)
    PostDto read(Long id);

    // 게시물 수정 (본인만 가능)
    boolean update(PostDto postDto, Long userId);

    // 게시물 삭제 (본인만 가능)
    boolean delete(Long id, Long userId);

    // 게시물 목록 조회 (페이징, 게시판 필터)
    Page<PostDto> getList(Long boardId, Pageable pageable);

    // 내가 쓴 게시물 목록 조회 (마이페이지)
    Page<PostDto> getListByUser(Long userId, Pageable pageable);
}

