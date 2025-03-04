package dev.services.order;

import dev.account.user.AddressDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Nelson Tanko
 */
public class OrderDTO {
    private OrderDTO() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotEmpty(message = "Order items cannot be empty")
        @Valid
        private List<OrderItemDTO.Request> orderItems;

        private AddressDTO.Request deliveryAddress;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long userId;
        private String userName;
        private Long restaurantId;
        private String restaurantName;
        private List<OrderItemDTO.Response> orderItems;
        private BigDecimal totalAmount;
        private AddressDTO.Response deliveryAddress;
        private LocalDateTime orderTime;
        private LocalDateTime estimatedDeliveryTime;
        private OrderStatus status;
    }
}
