package com.example.mlbf.controller.Board;

import com.example.mlbf.service.Board.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * 게시판 목록 조회 (비회원 가능)
     * GET /boards
     */
    @GetMapping("/boards")
    public ResponseEntity<?> getBoardList() {
        log.info("getBoardList");
        return ResponseEntity.ok(boardService.getBoardList());
    }

}
