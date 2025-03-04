package dev.services.food;

import dev.services.comment.CommentDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Nelson Tanko
 */
public class FoodDTO {
    private FoodDTO() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long id;

        @NotBlank(message = "Food name is required")
        private String name;

        private String description;

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        private BigDecimal price;

        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private String imageUrl;
        private boolean available;
        private Double averageRating;
        private Integer totalRatings;
        private List<CommentDTO.Response> comments;
    }

    @Data
    @Builder
    public static class FilterRequest{
        private Boolean available;
        private Integer totalRatings;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Double minRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String name;

        private String description;

        @Positive(message = "Price must be positive")
        private BigDecimal price;

        private boolean available;

        private String imageUrl;
    }
}