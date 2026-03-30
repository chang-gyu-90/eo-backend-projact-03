package com.example.mlbf.service.Comment;

import com.example.mlbf.domain.Comment.CommentDto;

import java.util.List;

public interface CommentService {

    // 댓글 작성
    CommentDto create(Long postId, Long userId, CommentDto commentDto);

    // 댓글 목록 조회 (비회원 가능)
    List<CommentDto> getList(Long postId);

    // 댓글 수정 (본인만 가능)
    boolean update(Long commentId, Long userId, CommentDto commentDto);

    // 댓글 삭제 (본인만 가능)
    boolean delete(Long commentId, Long userId);

    // 내가 쓴 댓글 목록 조회 (마이페이지)
    List<CommentDto> getListByUser(Long userId);
}
