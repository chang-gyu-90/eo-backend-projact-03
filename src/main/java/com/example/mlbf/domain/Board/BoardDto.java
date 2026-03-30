package com.example.mlbf.domain.Board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoardDto {

    private Long id;

    @NotBlank(message = "게시판 이름을 입력해야 합니다.")
    @Size(max = 20, message = "게시판 이름은 20자 이하여야 합니다.")
    private String name;

    // Entity -> Dto 변환
    public static BoardDto from(BoardEntity boardEntity) {
        return BoardDto.builder()
                .id(boardEntity.getId())
                .name(boardEntity.getName())
                .build();
    }
}
