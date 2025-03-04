package dev.services.food;

import dev.core.spec.AbstractSpecificationBuilder;
import dev.services.food.FoodDTO.FilterRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Nelson Tanko
 */
@Component
public class FoodSpecificationBuilder extends AbstractSpecificationBuilder<Food, FilterRequest> {

    @Override
    protected List<Specification<Food>> preparedSpecifications(FilterRequest filterRequest) {
        List<Specification<Food>> specifications = new ArrayList<>();

        if (filterRequest == null) {
            return Collections.emptyList();
        }
        if (filterRequest.getAvailable() != null){
            specifications.add(columnEquals("available", filterRequest.getAvailable()));
        }
        if (filterRequest.getMaxPrice() != null){
            specifications.add(columnLessThanOrEqual("price", filterRequest.getMaxPrice()));
        }
        if (filterRequest.getMinPrice() != null){
            specifications.add(columnGreaterThanOrEqual("price", filterRequest.getMinPrice()));
        }
        if (filterRequest.getMinRating() != null){
            specifications.add(columnGreaterThanOrEqual("averageRating", filterRequest.getMinRating()));
        }
        return specifications;
    }
}
