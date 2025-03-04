package dev.services.restaurant;

import dev.services.restaurant.RestaurantDTO.Request;
import dev.services.restaurant.RestaurantDTO.Response;
import dev.services.restaurant.RestaurantDTO.UpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author Nelson Tanko
 */
@RestController
@RequestMapping("/api/restaurant")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    /**
     * Create a new restaurant
     * Only admin can create restaurants
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> createRestaurant(@Valid @RequestBody Request requestDTO) {
        Response createdRestaurant = restaurantService.createRestaurant(requestDTO);
        return new ResponseEntity<>(createdRestaurant, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<Response>> getAllRestaurants(@PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(restaurantService.getAllRestaurants(pageable));
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<Response> getRestaurantById(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(restaurantId));
    }

    @PatchMapping("/{restaurantId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> updateRestaurant(@PathVariable Long restaurantId, @Valid @RequestBody UpdateRequest request) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(restaurantId, request));
    }

    @DeleteMapping("/{restaurantId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long restaurantId) {
        restaurantService.deleteRestaurant(restaurantId);
        return ResponseEntity.noContent().build();
    }
}
