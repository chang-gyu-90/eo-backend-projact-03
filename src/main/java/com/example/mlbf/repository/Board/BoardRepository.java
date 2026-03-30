package com.example.mlbf.repository.Board;

import com.example.mlbf.domain.Board.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    // 게시판 이름 중복 체크 (생성 시)
    boolean existsByName(String name);

    // 게시판 이름으로 조회
    Optional<BoardEntity> findByName(String name);
}
