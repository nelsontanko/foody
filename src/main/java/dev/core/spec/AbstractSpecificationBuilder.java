package dev.core.spec;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.*;
import java.util.function.BiFunction;

public abstract class AbstractSpecificationBuilder<T, K> {

    protected abstract List<Specification<T>> preparedSpecifications(K k);

    public Specification<T> buildSpecification(K k) {
        List<Specification<T>> specifications = preparedSpecifications(k);
        if (!specifications.isEmpty()) {
            return specifications.stream()
                    .reduce(Specification::and)
                    .orElse(null);
        }
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.and();
    }

    protected String getAsteriskSearchString(String value) {
        return value != null ? "%" + value.replace("*", "%") + "%" : "%%";
    }

    protected String likeValue(String value) {
        return value != null ? "%" + value + "%" : "%%";
    }

    protected void addIfPresent(List<Specification<T>> specifications, String column, String value,
        BiFunction<String, String, Specification<T>> specificationFunction) {
        if (StringUtils.isNotEmpty(value)) {
            specifications.add(specificationFunction.apply(column, value));
        }
    }

    protected void addIfPresent(List<Specification<T>> specifications, String column, Object value,
        BiFunction<String, Object, Specification<T>> specificationFunction) {
        if (Objects.nonNull(value)) {
            specifications.add(specificationFunction.apply(column, value));
        }
    }

    protected Specification<T> columnGreaterThan(String columnName, BigDecimal value) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
            .greaterThan(root.get(columnName).as(BigDecimal.class), value);
    }

    protected Specification<T> columnLike(String columnName, String value) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.like(root.get(columnName), likeValue(value));
    }


    protected Specification<T> columnLikeLowerCase(String columnName, String value) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
            .like(criteriaBuilder.lower(root.get(columnName)), likeValue(value.toLowerCase()));
    }

    protected Specification<T> columnLikeFuzzyMatch(String columnName, String values) {
        return (root, queryBuilder, criteriaBuilder) -> {
            String[] words = values.split("\\s+"); // Split query into words
            List<Predicate> predicates = Arrays.stream(words)
                    .map(word -> criteriaBuilder.like(criteriaBuilder.lower(root.get(columnName)), "%" + word.toLowerCase() + "%"))
                    .toList();

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    protected Specification<T> columnLikeUpperCase(String columnName, String value) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
            .like(criteriaBuilder.upper(root.get(columnName)), likeValue(value.toUpperCase()));
    }

    protected Specification<T> columnBetweenDates(String columnName, long startTime, long endTime) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.between(root.get(columnName),
            getStartDate(new Date(startTime)), getEndDate(new Date(endTime)));
    }

    protected Specification<T> columnBetweenLocalDateTime(String columnName, long date) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.between(root.get(columnName),
            getStartDate(new Date(date)).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
            getEndDate(new Date(date)).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    protected Specification<T> columnLessThan(String columnName, Long value) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.lessThan(root.get(columnName).as(Long.class),
            value);
    }

    protected Specification<T> columnLessThan(String columnName, BigDecimal value) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
            .lessThan(root.get(columnName).as(BigDecimal.class), value);
    }

    protected Specification<T> columnGreaterThanOrEqual(String columnName, Double value) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
            .greaterThanOrEqualTo(root.get(columnName).as(Double.class), value);
    }

    protected Specification<T> columnGreaterThanOrEqual(String columnName, BigDecimal value) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
            .greaterThanOrEqualTo(root.get(columnName).as(BigDecimal.class), value);
    }

    protected Specification<T> columnLessThanOrEqual(String columnName, Long value) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
            .lessThanOrEqualTo(root.get(columnName).as(Long.class), value);
    }

    protected Specification<T> columnLessThanOrEqual(String columnName, BigDecimal value) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
            .lessThanOrEqualTo(root.get(columnName).as(BigDecimal.class), value);
    }

    protected Specification<T> columnEquals(String columnName, Object value) {
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(columnName), value);
    }

    protected Specification<T> columnNameEquals(String joinColumn, String value, String columnName) {
        return (root, query, criteriaBuilder) -> {
            Join<T, T> join = root.join(joinColumn);
            return criteriaBuilder.equal(criteriaBuilder.lower(join.get(value)), columnName.toLowerCase());
        };
    }

    protected Specification<T> columnNotEqual(String columnName, Object value) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.notEqual(root.get(columnName), value);
    }

    protected Specification<T> columnBetween(String columnName, Integer minValue, Integer maxValue) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.between(
            root.get(columnName).as(Integer.class), minValue, maxValue);
    }

    protected Specification<T> columnBetween(String columnName, BigDecimal minValue, BigDecimal maxValue) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.between(
            root.get(columnName).as(BigDecimal.class), minValue, maxValue);
    }

    public Pageable sortValid(Pageable pageable, Map<String, String> allowedSortFields) {

        // Validating sorting parameters passed in the request are valid
        pageable.getSort().iterator().forEachRemaining(order -> {
            boolean isValidField = allowedSortFields.containsKey(order.getProperty());
            if (!isValidField) {
                throw new IllegalArgumentException("Invalid sorting criteria! value: " + order.getProperty());
            }
        });

        List<Sort.Order> orderList = new ArrayList<>();
        Iterator<Sort.Order> orders = pageable.getSort().iterator();
        int count = 0;

        while (orders.hasNext()) {
            Sort.Order order = orders.next();
            count = count + 1;
            // Replacing sorting parameters passed in the request to the actual column name
            for (Map.Entry<String, String> entry : allowedSortFields.entrySet()) {
                if (order.getProperty().equalsIgnoreCase(entry.getKey())) {
                    orderList.add(new Sort.Order(order.getDirection(), entry.getValue()));
                }
            }
        }

        if (orderList.isEmpty() || orderList.size() != count) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid sorting criteria");
        }

        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orderList));
        return pageable;
    }

    private Date getStartDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getEndDate(Date date) {

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);
        cal2.add(Calendar.DATE, 1);

        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        return cal2.getTime();
    }

}
