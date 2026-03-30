package com.example.mlbf.domain.Post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDto {

    private Long id;

    @NotBlank(message = "카테고리는 필수 입력 항목입니다.")
    @Size(max = 100, message = "카테고리는 100자 이하여야 합니다.")
    private String category;

    @NotBlank(message = "제목과 내용은 반드시 입력되어야 합니다.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "제목과 내용은 반드시 입력되어야 합니다.")
    private String content;

    private Integer likes;
    private Integer views;
    private Integer commentCount;   // 댓글 수 (게시물 목록에서 표시)

    private Long userId;        // 작성자 id
    private String nickname;    // 작성자 닉네임
    private Long boardId;       // 게시판 id
    private String boardName;   // 게시판 이름

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> Dto 변환
    public static PostDto from(PostEntity postEntity) {
        return PostDto.builder()
                .id(postEntity.getId())
                .category(postEntity.getCategory())
                .title(postEntity.getTitle())
                .content(postEntity.getContent())
                .likes(postEntity.getLikes())
                .views(postEntity.getViews())
                .userId(postEntity.getUser().getId())
                .nickname(postEntity.getUser().getNickname())
                .boardId(postEntity.getBoard().getId())
                .boardName(postEntity.getBoard().getName())
                .createdAt(postEntity.getCreatedAt())
                .updatedAt(postEntity.getUpdatedAt())
                .build();
    }

}
