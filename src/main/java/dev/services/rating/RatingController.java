package dev.services.rating;

import dev.services.rating.RatingDTO.Request;
import dev.services.rating.RatingDTO.Response;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Nelson Tanko
 */
@Controller
@RequestMapping("/api/rating")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<Response> rateFood(@Valid @RequestBody Request ratingDTO) {
        Response savedRating = ratingService.addOrUpdateRating(ratingDTO);
        return ResponseEntity.ok(savedRating);
    }
}
