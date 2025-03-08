package dev.services.order;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Nelson Tanko
 */
@Data
@AllArgsConstructor
public class OrderStatusUpdateRequest {
    private Long orderId;
    private OrderStatus orderStatus;
}
