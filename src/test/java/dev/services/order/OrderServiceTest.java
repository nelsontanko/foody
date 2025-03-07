package dev.services.order;

import dev.core.exception.GenericApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * @author Nelson Tanko
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setTotalAmount(BigDecimal.TWO);
        testOrder.setStatus(OrderStatus.PENDING);
    }

    @Test
    void updateOrderStatus_SuccessfulUpdate() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        orderService.updateOrderStatus(1L, OrderStatus.PREPARING);

        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.PREPARING);
        verify(orderRepository).save(testOrder);
        verify(messagingTemplate).convertAndSend("/topic/order-tracking/1", OrderStatus.PREPARING);
    }

    @Test
    void updateOrderStatus_OrderNotFound_ShouldThrowException() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(2L, OrderStatus.DELIVERED))
                .isInstanceOf(GenericApiException.class)
                .hasMessage("api.order.notFound.");

        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void updateOrderStatus_AlreadyDelivered_ShouldThrowException() {
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OrderStatus.CANCELLED))
                .isInstanceOf(GenericApiException.class)
                .hasMessage("Cannot update a delivered order.");

        verify(orderRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

}
