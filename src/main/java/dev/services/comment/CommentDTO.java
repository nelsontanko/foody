package dev.services.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @author Nelson Tanko
 */
public class CommentDTO {
    private CommentDTO() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request{
        @NotNull(message = "Food ID is required")
        private Long foodId;

        @NotBlank(message = "Comment cannot be empty")
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        private Long id;

        private Long foodId;

        private Long userId;

        private String content;

        private String username;
        private LocalDateTime createdAt;
    }

}