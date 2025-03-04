package dev.services.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Nelson Tanko
 */


public class RatingDTO {
    private RatingDTO() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request{

        @NotNull(message = "Food ID is required")
        private Long foodId;

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be between 1 and 5")
        @Max(value = 5, message = "Rating must be between 1 and 5")
        private Integer rating;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        private Long id;
        private Long foodId;
        private Long userId;
        private Integer rating;
        private String username;
        private LocalDateTime createdAt;
    }
}
