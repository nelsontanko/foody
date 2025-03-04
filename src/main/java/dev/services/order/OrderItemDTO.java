package dev.services.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

/**
 * @author Nelson Tanko
 */
public class OrderItemDTO {
    private OrderItemDTO() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "Food ID is required")
        private Long foodId;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private int quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long foodId;
        private String foodName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
    }
}