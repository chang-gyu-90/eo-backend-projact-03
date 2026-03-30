package com.example.mlbf.controller.Post;

import com.example.mlbf.domain.Post.PostDto;
import com.example.mlbf.domain.User.UserDto;
import com.example.mlbf.security.CustomUserDetails;
import com.example.mlbf.service.Board.BoardService;
import com.example.mlbf.service.Post.PostService;
import com.example.mlbf.service.User.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;
    private final BoardService boardService;

    /**
     * 게시물 목록 조회 (비회원 가능)
     * GET /board/list?page=1&board_id=1
     */
    @GetMapping("/board/list")
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Long boardId) {
        log.info("list: page = {}, boardId = {}", page, boardId);

        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<PostDto> postDtoPage = postService.getList(boardId, pageable);

        return ResponseEntity.ok(postDtoPage);
    }

    /**
     * 게시물 상세 조회 (비회원 가능)
     * GET /board/read?id=1
     */
    @GetMapping("/board/read")
    public ResponseEntity<?> read(@RequestParam Long id) {
        log.info("read: id = {}", id);

        try {
            PostDto postDto = postService.read(id);
            return ResponseEntity.ok(postDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 게시물 작성 (로그인 필요)
     * POST /board/write
     */
    @PostMapping("/board/write")
    public ResponseEntity<?> write(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostDto postDto) {
        log.info("write: postDto = {}", postDto);

        try {
            // userId(String)로 사용자 조회 후 id(Long) 가져오기
            String currentUserId = userDetails.getUsername();
            UserDto currentUser = userService.read(currentUserId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
            Long userId = currentUser.getId();

            postService.create(postDto, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "게시물이 등록되었습니다.", "id", postDto.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 게시물 수정 (본인만 가능)
     * POST /board/update
     */
    @PostMapping("/board/update")
    public ResponseEntity<?> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostDto postDto) {
        log.info("update: postDto = {}", postDto);

        try {
            // userId(String)로 사용자 조회 후 id(Long) 가져오기
            String currentUserId = userDetails.getUsername();
            UserDto currentUser = userService.read(currentUserId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
            Long userId = currentUser.getId();

            boolean updated = postService.update(postDto, userId);
            if (updated) {
                return ResponseEntity.ok(Map.of("message", "게시물이 수정되었습니다."));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "존재하지 않거나 삭제된 게시물입니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 게시물 삭제 (본인만 가능)
     * POST /board/delete
     */
    @PostMapping("/board/delete")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, Long> request) {
        log.info("delete: id = {}", request.get("id"));

        try {
            // userId(String)로 사용자 조회 후 id(Long) 가져오기
            String currentUserId = userDetails.getUsername();
            UserDto currentUser = userService.read(currentUserId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
            Long userId = currentUser.getId();

            Long id = request.get("id");
            boolean deleted = postService.delete(id, userId);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "게시물이 삭제되었습니다."));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "존재하지 않거나 삭제된 게시물입니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}