package dev.services.food;

import dev.core.exception.GenericApiException;
import dev.services.food.FoodDTO.Request;
import dev.services.food.FoodDTO.Response;
import dev.services.food.FoodDTO.UpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodServiceTest {

    @Mock FoodRepository foodRepository;

    @Mock FoodMapper foodMapper;
    @InjectMocks FoodService foodService;

    private Food food;
    private Request foodRequest;
    private UpdateRequest updateFoodRequest;
    private Response foodResponse;

    @BeforeEach
    void setUp() {
        food = Food.builder()
                .id(1L)
                .name("Pizza Margherita")
                .description("Classic Italian pizza")
                .price(new BigDecimal("12.99"))
                .available(true)
                .build();

        foodRequest = Request.builder()
                .name("Pizza Margherita")
                .description("Classic Italian pizza")
                .price(new BigDecimal("12.99"))
                .build();

        updateFoodRequest = UpdateRequest.builder()
                .name("Pizza Margherita")
                .description("Classic Italian pizza")
                .price(new BigDecimal("12.99"))
                .build();

        foodResponse = Response.builder()
                .id(1L)
                .name("Pizza Margherita")
                .description("Classic Italian pizza")
                .price(new BigDecimal("12.99"))
                .available(true)
                .build();
    }

    @Test
    void addFood_Success() {
        // Given
        when(foodRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(foodMapper.toEntity(any(Request.class))).thenReturn(food);
        when(foodRepository.save(any(Food.class))).thenReturn(food);
        when(foodMapper.toDto(any(Food.class))).thenReturn(foodResponse);

        // When
        Response result = foodService.addFood(foodRequest);

        // Then
        assertNotNull(result);
        assertEquals(foodResponse.getId(), result.getId());
        assertEquals(foodResponse.getName(), result.getName());

        verify(foodRepository, times(1)).findByName(foodRequest.getName());
        verify(foodMapper, times(1)).toEntity(foodRequest);
        verify(foodRepository, times(1)).save(food);
        verify(foodMapper, times(1)).toDto(food);
    }

    @Test
    void addFood_DuplicateName_ThrowsException() {
        // Given
        when(foodRepository.findByName(anyString())).thenReturn(Optional.of(food));

        // When & Then
        assertThrows(GenericApiException.class, () -> foodService.addFood(foodRequest));

        verify(foodRepository, times(1)).findByName(foodRequest.getName());
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void updateFood_Success() {
        // Given
        when(foodRepository.findById(anyLong())).thenReturn(Optional.of(food));
        when(foodRepository.save(any(Food.class))).thenReturn(food);
        when(foodMapper.toDto(any(Food.class))).thenReturn(foodResponse);

        // When
        Response result = foodService.updateFood(1L, updateFoodRequest);

        // Then
        assertNotNull(result);
        assertEquals(foodResponse.getId(), result.getId());

        verify(foodRepository, times(1)).findById(1L);
        verify(foodMapper, times(1)).updateFoodFromDto(updateFoodRequest, food);
        verify(foodRepository, times(1)).save(food);
        verify(foodMapper, times(1)).toDto(food);
    }

    @Test
    void updateFood_NotFound_ThrowsException() {
        // Given
        when(foodRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(GenericApiException.class, () -> foodService.updateFood(1L, updateFoodRequest));

        verify(foodRepository, times(1)).findById(1L);
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void deleteFood_Success() {
        // Given
        when(foodRepository.findById(anyLong())).thenReturn(Optional.of(food));

        // When
        foodService.deleteFood(1L);

        // Then
        verify(foodRepository, times(1)).findById(1L);
        verify(foodRepository, times(1)).delete(food);
    }

    @Test
    void deleteFood_NotFound_ThrowsException() {
        // Given
        when(foodRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(GenericApiException.class, () -> foodService.deleteFood(1L));

        verify(foodRepository, times(1)).findById(1L);
        verify(foodRepository, never()).delete(any(Food.class));
    }

    @Test
    void getFoodById_Success() {
        // Given
        when(foodRepository.findById(anyLong())).thenReturn(Optional.of(food));
        when(foodMapper.toDto(any(Food.class))).thenReturn(foodResponse);

        // When
        Response result = foodService.getFoodById(1L, 1);

        // Then
        assertNotNull(result);
        assertEquals(foodResponse.getId(), result.getId());

        verify(foodRepository, times(1)).findById(1L);
        verify(foodMapper, times(1)).toDto(food);
    }

    @Test
    void getFoodById_NotFound_ThrowsException() {
        // Given
        when(foodRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(GenericApiException.class, () -> foodService.getFoodById(1L, 1));

        verify(foodRepository, times(1)).findById(1L);
        verify(foodMapper, never()).toDto(any(Food.class));
    }
}