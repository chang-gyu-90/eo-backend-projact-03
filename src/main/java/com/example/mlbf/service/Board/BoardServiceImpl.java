package com.example.mlbf.service.Board;

import com.example.mlbf.domain.Board.BoardDto;
import com.example.mlbf.domain.Board.BoardEntity;
import com.example.mlbf.repository.Board.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;

    @Override
    public List<BoardDto> getBoardList() {
        log.info("getBoardList");
        return boardRepository.findAll()
                .stream()
                .map(BoardDto::from)
                .toList();
    }

    @Override
    public void createBoard(BoardDto boardDto) {
        log.info("createBoard: boardDto = {}", boardDto);

        if (boardRepository.existsByName(boardDto.getName())) {
            throw new IllegalArgumentException("이미 존재하는 게시판입니다.");
        }

        BoardEntity savedEntity = boardRepository.save(BoardEntity.from(boardDto));
        boardDto.setId(savedEntity.getId());
    }
}
