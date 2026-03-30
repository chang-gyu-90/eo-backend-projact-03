package com.example.mlbf.domain.Comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDto {

    private Long id;

    private Long postId;        // 게시물 id

    private Long userId;        // 작성자 id
    private String nickname;    // 작성자 닉네임

    @NotBlank(message = "댓글 내용을 입력해야 합니다.")
    @Size(min = 1, max = 1000, message = "댓글은 1자 이상 1000자 이하로 작성해야 합니다.")
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> Dto 변환
    public static CommentDto from(CommentEntity commentEntity) {
        return CommentDto.builder()
                .id(commentEntity.getId())
                .postId(commentEntity.getPost().getId())
                .userId(commentEntity.getUser().getId())
                .nickname(commentEntity.getUser().getNickname())
                .content(commentEntity.getContent())
                .createdAt(commentEntity.getCreatedAt())
                .updatedAt(commentEntity.getUpdatedAt())
                .build();
    }
}
