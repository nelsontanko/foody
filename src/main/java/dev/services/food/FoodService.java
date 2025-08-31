package dev.services.food;

import dev.core.exception.ErrorCode;
import dev.core.exception.GenericApiException;
import dev.services.comment.CommentDTO;
import dev.services.common.CacheService;
import dev.services.food.FoodDTO.Request;
import dev.services.food.FoodDTO.Response;
import dev.services.food.FoodDTO.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dev.core.exception.ErrorCode.FOOD_NOT_FOUND;

/**
 * @author Nelson Tanko
 */
@Service
public class FoodService {
    private static final String CACHE_NAME = "food";

    private static final Logger LOG = LoggerFactory.getLogger(FoodService.class);

    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;
    private final CacheService cacheService;
    private final FoodSpecificationBuilder foodSpecificationBuilder;

    public FoodService(FoodRepository foodRepository, FoodMapper foodMapper, CacheService cacheService, FoodSpecificationBuilder foodSpecificationBuilder) {
        this.foodRepository = foodRepository;
        this.foodMapper = foodMapper;
        this.cacheService = cacheService;
        this.foodSpecificationBuilder = foodSpecificationBuilder;
    }

    @Transactional
    public Response addFood(Request request){
        LOG.info("Adding new Food {}", request.getName());

        foodRepository.findByName(request.getName()).
                ifPresent(food -> {
                    throw new GenericApiException(ErrorCode.FOOD_ALREADY_EXISTS, new Object[]{food.getName()});
                });
        Food food = foodMapper.toEntity(request);
        Food saveFood = foodRepository.save(food);
        cacheService.evictAllCacheEntries(CACHE_NAME);
        LOG.info("Food added successfully with id: {}", saveFood.getId());
        return foodMapper.toDto(saveFood);
    }

    public Response updateFood(Long foodId, UpdateRequest request) {
        LOG.info("Updating Food with id: {}", foodId);

        Food existingFood = findFoodById(foodId);

        // Check name uniqueness if name is changed
        if (!existingFood.getName().equals(request.getName())) {
            foodRepository.findByName(request.getName()).
                    ifPresent(food -> {
                        throw new GenericApiException(ErrorCode.FOOD_ALREADY_EXISTS, new Object[]{food.getName()});
                    });
        }
        foodMapper.updateFoodFromDto(request, existingFood);
        Food updatedFood = foodRepository.save(existingFood);
        cacheService.evictAllCacheEntries(CACHE_NAME);

        LOG.info("Food updated successfully with id: {}", updatedFood.getId());
        return foodMapper.toDto(updatedFood);
    }

    @Transactional
    public void deleteFood(Long foodId) {
        LOG.info("Deleting Food with id: {}", foodId);

        Food food = findFoodById(foodId);
        food.setActive(false);

        foodRepository.save(food);
        cacheService.evictAllCacheEntries(CACHE_NAME);
        LOG.info("Food deleted successfully with id: {}", foodId);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_NAME)
    public Response getFoodById(Long foodId, int commentCount) {
        LOG.info("Fetching food details with id: {}", foodId);
        Food food = findFoodById(foodId);

        Response response = foodMapper.toDto(food);
        return limitCommentInResponse(response, commentCount);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_NAME)
    public Page<Response> getAllFood(Pageable pageable, FoodDTO.FilterRequest filterRequest, int commentCount) {
        LOG.info("Fetching all foods with {} comments per food", commentCount);
        Specification<Food> foodSpecification = foodSpecificationBuilder.buildSpecification(filterRequest);

        Page<Food> foods = foodRepository.findAll(foodSpecification, pageable);

        Page<Response> response = foods.map(foodMapper::toDto);

        return response.map(res -> limitCommentInResponse(res, commentCount));
    }

    private Response limitCommentInResponse(Response response, int commentCount) {
        if (response.getComments() != null && response.getComments().size() > commentCount){
            List<CommentDTO.Response> comments = response.getComments().stream()
                    .limit(commentCount)
                    .toList();
            response.setComments(comments);
        }
        return response;
    }

    private Food findFoodById(Long id) {
        return foodRepository.findById(id)
                .filter(Food::isAvailable)
                .orElseThrow(() -> new GenericApiException(FOOD_NOT_FOUND));
    }
}
