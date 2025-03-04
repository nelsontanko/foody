package dev.services.order;

import dev.account.user.AddressMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @author Nelson Tanko
 */
@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.fullname")
    @Mapping(target = "restaurantId", source = "restaurant.id")
    @Mapping(target = "restaurantName", source = "restaurant.name")
    @Mapping(target = "deliveryAddress", source = "deliveryAddress")
    OrderDTO.Response toDto(Order order);

    List<OrderDTO.Response> toDtoList(List<Order> orders);

    @Mapping(target = "foodId", source = "food.id")
    @Mapping(target = "foodName", source = "food.name")
    @Mapping(target = "subtotal", expression = "java(orderItem.getSubtotal())")
    OrderItemDTO.Response orderItemToDto(OrderItem orderItem);

    List<OrderItemDTO.Response> orderItemsToDtoList(List<OrderItem> orderItems);
}
