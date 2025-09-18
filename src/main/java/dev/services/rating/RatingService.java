package dev.services.rating;

import dev.account.user.User;
import dev.core.exception.ErrorCode;
import dev.core.exception.GenericApiException;
import dev.services.common.CacheService;
import dev.services.food.Food;
import dev.services.food.FoodRepository;
import dev.services.rating.RatingDTO.Request;
import dev.services.rating.RatingDTO.Response;
import dev.services.util.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nelson Tanko
 */
@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final FoodRepository foodRepository;
    private final RatingMapper ratingMapper;
    private final CacheService cacheService;
    private final AuthenticatedUser auth;

    public RatingService(RatingRepository ratingRepository, FoodRepository foodRepository, RatingMapper ratingMapper, CacheService cacheService, AuthenticatedUser auth) {
        this.ratingRepository = ratingRepository;
        this.foodRepository = foodRepository;
        this.ratingMapper = ratingMapper;
        this.cacheService = cacheService;
        this.auth = auth;
    }

    @Transactional
    public Response addOrUpdateRating(Request request) {
        User user = auth.getAuthenticatedUser();
        Food food = getFoodById(request.getFoodId());

        Rating rating = ratingRepository.findByFoodIdAndUserId(request.getFoodId(), user.getId())
                .map(existingRating -> updateRating(existingRating, request.getRating()))
                .orElseGet(() -> creatNewRating(request, user, food));

        food.calculateAverageRating();
        foodRepository.save(food);
        cacheService.evictAllCacheEntries("food");
        return ratingMapper.toDto(rating);
    }

    private Food getFoodById(Long foodId) {
        return foodRepository.findById(foodId)
                .orElseThrow(() -> new GenericApiException(ErrorCode.FOOD_NOT_FOUND));
    }

    private Rating creatNewRating(Request request, User user, Food food) {
        Rating rating = ratingMapper.toEntity(request);
        rating.setUser(user);
        rating.setFood(food);
        return ratingRepository.save(rating);
    }

    private Rating updateRating(Rating rating, Integer newRating) {
        rating.setRating(newRating);
        return ratingRepository.save(rating);
    }
}