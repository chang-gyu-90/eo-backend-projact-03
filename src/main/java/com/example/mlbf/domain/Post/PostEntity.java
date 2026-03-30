package com.example.mlbf.domain.Post;

import com.example.mlbf.domain.Board.BoardEntity;
import com.example.mlbf.domain.Comment.CommentEntity;
import com.example.mlbf.domain.User.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "posts")
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank(message = "카테고리는 필수 입력 항목입니다.")
    @Size(max = 100, message = "카테고리는 100자 이하여야 합니다.")
    @Column(name = "category", length = 100, nullable = false)
    private String category;

    @NotBlank(message = "제목과 내용은 반드시 입력되어야 합니다.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @NotBlank(message = "제목과 내용은 반드시 입력되어야 합니다.")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "likes", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int likes = 0;

    @Column(name = "views", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int views = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    @ToString.Exclude
    private BoardEntity board;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<CommentEntity> comments = new ArrayList<>();

    @Builder
    public PostEntity(String category, String title, String content,
                      UserEntity user, BoardEntity board) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.likes = 0;
        this.views = 0;
        this.user = user;
        this.board = board;
    }

    // 게시물 수정
    public PostEntity update(String category, String title, String content) {
        this.category = category;
        this.title = title;
        this.content = content;
        return this;
    }

    // 조회수 증가
    public PostEntity increaseViews() {
        this.views++;
        return this;
    }

    // 좋아요 증가
    public PostEntity increaseLikes() {
        this.likes++;
        return this;
    }

    // 좋아요 감소
    public PostEntity decreaseLikes() {
        if (this.likes > 0) this.likes--;
        return this;
    }

    // DTO로부터 엔티티 생성
    public static PostEntity from(PostDto postDto, UserEntity user, BoardEntity board) {
        return PostEntity.builder()
                .category(postDto.getCategory())
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .user(user)
                .board(board)
                .build();
    }
}
