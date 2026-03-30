package com.example.mlbf.service.Board;

import com.example.mlbf.domain.Board.BoardDto;

import java.util.List;

public interface BoardService {
    List<BoardDto> getBoardList();
    void createBoard(BoardDto boardDto);
}

