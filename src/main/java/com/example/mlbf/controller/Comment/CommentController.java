package com.example.mlbf.controller.Comment;

import com.example.mlbf.domain.Comment.CommentDto;
import com.example.mlbf.domain.User.UserDto;
import com.example.mlbf.security.CustomUserDetails;
import com.example.mlbf.service.Comment.CommentService;
import com.example.mlbf.service.User.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/board/{id}/comment")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    /**
     * 댓글 목록 조회 (비회원 가능)
     * GET /board/{id}/comment/list
     */
    @GetMapping("/list")
    public ResponseEntity<?> getList(@PathVariable Long id) {
        log.info("getList: postId = {}", id);

        try {
            List<CommentDto> commentDtoList = commentService.getList(id);
            return ResponseEntity.ok(commentDtoList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 댓글 작성 (로그인 필요)
     * POST /board/{id}/comment/write
     */
    @PostMapping("/write")
    public ResponseEntity<?> write(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CommentDto commentDto) {
        log.info("write: postId = {}, commentDto = {}", id, commentDto);

        try {
            String currentUserId = userDetails.getUsername();
            UserDto currentUser = userService.read(currentUserId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
            Long userId = currentUser.getId();

            CommentDto savedDto = commentService.create(id, userId, commentDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(savedDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 댓글 수정 (본인만 가능)
     * POST /board/{id}/comment/update
     */
    @PostMapping("/update")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CommentDto commentDto) {
        log.info("update: postId = {}, commentDto = {}", id, commentDto);

        try {
            String currentUserId = userDetails.getUsername();
            UserDto currentUser = userService.read(currentUserId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
            Long userId = currentUser.getId();

            boolean updated = commentService.update(commentDto.getId(), userId, commentDto);
            if (updated) {
                return ResponseEntity.ok(Map.of("message", "댓글이 수정되었습니다."));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "존재하지 않거나 삭제된 댓글입니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 댓글 삭제 (본인만 가능)
     * POST /board/{id}/comment/delete
     */
    @PostMapping("/delete")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, Long> request) {
        log.info("delete: postId = {}, commentId = {}", id, request.get("comment_id"));

        try {
            String currentUserId = userDetails.getUsername();
            UserDto currentUser = userService.read(currentUserId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
            Long userId = currentUser.getId();
            Long commentId = request.get("comment_id");

            boolean deleted = commentService.delete(commentId, userId);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "댓글이 삭제되었습니다."));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "존재하지 않거나 삭제된 댓글입니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}
