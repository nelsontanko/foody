package dev.services.food;

import dev.services.food.FoodDTO.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * @author Nelson Tanko
 */
@RestController
@RequestMapping("/api/food")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> addFood(@Valid @RequestBody Request request) {
        Response response = foodService.addFood(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{foodId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> updateFood(@PathVariable Long foodId, @Valid @RequestBody UpdateRequest request) {
        Response response = foodService.updateFood(foodId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{foodId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteFood(@PathVariable Long foodId) {
        foodService.deleteFood(foodId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{foodId}")
    public ResponseEntity<Response> getFoodById(@PathVariable Long foodId,
                                                @RequestParam(defaultValue = "1", name = "comment_count")
                                                int commentCount) {
        return ResponseEntity.ok(foodService.getFoodById(foodId, commentCount));
    }

    @GetMapping
    public ResponseEntity<Page<Response>> getAllFood(@PageableDefault(sort = {"averageRating", "createdDate"},
                                                     direction = Sort.Direction.DESC) final Pageable pageable ,
                                                     @RequestParam(defaultValue = "1", name = "comment_count") int commentCount,
                                                     @RequestParam(name = "available", required = false) Boolean available,
                                                     @RequestParam(name = "min_price", required = false) BigDecimal minPrice,
                                                     @RequestParam(name = "max_price", required = false) BigDecimal maxPrice,
                                                     @RequestParam(name = "min_rating", required = false) Double minRating
    ) {
        FilterRequest request = FilterRequest.builder().available(available).minPrice(minPrice)
                .maxPrice(maxPrice).minRating(minRating).build();
        return ResponseEntity.ok(foodService.getAllFood(pageable, request, commentCount));
    }
}
