package dev.services.order;

import dev.services.order.OrderDTO.Request;
import dev.services.order.OrderDTO.Response;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;


/**
 * @author Nelson Tanko
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Response> createOrder(@Valid @RequestBody Request orderDto) {
        Response response = orderService.createOrder(orderDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<Response>> getUserOrders(@PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(orderService.getUserOrders(pageable));
    }

    @MessageMapping("/update-order-status")
    @SendTo("/topic/order-tracking")
    public ResponseEntity<Response> updateOrderStatus(OrderStatusUpdateDTO statusUpdateDTO) {
        Response response = orderService.updateOrderStatus(statusUpdateDTO.getOrderId(), statusUpdateDTO.getOrderStatus());
        return ResponseEntity.ok(response);
    }
}
