package dev.services.order;

import lombok.Data;

/**
 * @author Nelson Tanko
 */
@Data
public class OrderStatusUpdateDTO {
    private Long orderId;
    private OrderStatus orderStatus;
}
