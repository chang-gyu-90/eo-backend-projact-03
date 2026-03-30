package com.example.mlbf.domain.Board;

import com.example.mlbf.domain.Post.PostDto;
import com.example.mlbf.domain.Post.PostEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "boards")
public class BoardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank(message = "게시판 이름을 입력해야 합니다.")
    @Size(max = 20, message = "게시판 이름은 20자 이하여야 합니다.")
    @Column(name = "name", length = 20, nullable = false)
    private String name;

    // 게시판 삭제 시 하위 게시물도 함께 삭제
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude  // 순환 참조 방지
    private List<PostEntity> posts = new ArrayList<>();

    @Builder
    public BoardEntity(String name) {
        this.name = name;
    }

    // 게시판 이름 수정
    public BoardEntity updateName(String name) {
        this.name = name;
        return this;
    }

    // DTO로부터 엔티티 생성
    public static BoardEntity from(BoardDto boardDto) {
        return BoardEntity.builder()
                .name(boardDto.getName())
                .build();
    }
}
