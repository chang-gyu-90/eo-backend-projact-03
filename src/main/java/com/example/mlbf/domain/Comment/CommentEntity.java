package com.example.mlbf.domain.Comment;

import com.example.mlbf.domain.Post.PostEntity;
import com.example.mlbf.domain.User.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "comments")
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 댓글 작성자 (user 테이블 FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private UserEntity user;

    // 게시물 (posts 테이블 FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @ToString.Exclude
    private PostEntity post;

    @NotBlank(message = "댓글 내용을 입력해야 합니다.")
    @Size(min = 1, max = 1000, message = "댓글은 1자 이상 1000자 이하로 작성해야 합니다.")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public CommentEntity(UserEntity user, PostEntity post, String content) {
        this.user = user;
        this.post = post;
        this.content = content;
    }

    // 댓글 내용 수정
    public CommentEntity updateContent(String content) {
        this.content = content;
        return this;
    }

    // DTO로부터 엔티티 생성
    public static CommentEntity from(CommentDto commentDto, UserEntity user, PostEntity post) {
        return CommentEntity.builder()
                .user(user)
                .post(post)
                .content(commentDto.getContent())
                .build();
    }
}
