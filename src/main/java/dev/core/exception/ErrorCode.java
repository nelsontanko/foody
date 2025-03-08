package dev.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author Nelson Tanko
 */
@Getter
public enum ErrorCode {
    // General
    GENERAL_DEFAULT_ERROR_CODE("api.general.unknown", HttpStatus.BAD_REQUEST),
    GENERAL_NOT_FOUND_ERROR_CODE("api.general.notFound", HttpStatus.NOT_FOUND),
    GENERAL_UNAUTHORIZED_ACCESS("api.general.unauthorizedAccess", HttpStatus.UNAUTHORIZED),
    GENERAL_ACCESS_DENIED("api.general.accessDenied", HttpStatus.FORBIDDEN),
    GENERAL_UPLOAD_FILE_EMPTY("api.general.fileEmpty", HttpStatus.BAD_REQUEST),
    GENERAL_UPLOAD_FILE_IS_NOT_PHOTO("api.general.fileNotAPhoto", HttpStatus.BAD_REQUEST),
    // User
    USER_NOT_FOUND("api.user.notFound", HttpStatus.NOT_FOUND),
    USER_NOT_LOGGED_IN("api.user.notLoggedIn", HttpStatus.BAD_REQUEST),
    USER_ADDRESS_NOT_FOUND("api.user.addressNotFound", HttpStatus.NOT_FOUND),

    // Oder
    ORDER_NOT_FOUND("api.order.notFound", HttpStatus.NOT_FOUND),
    ORDER_CANNOT_BE_CANCELED("api.order.cannotBeCanceled", HttpStatus.CONFLICT),
    ORDER_ALREADY_DELIVERED( "api.order.alreadyDelivered", HttpStatus.BAD_REQUEST),

    // Restaurant
    RESTAURANT_NOT_FOUND("api.restaurant.notFound", HttpStatus.NOT_FOUND),
    RESTAURANT_ALREADY_EXISTS("api.restaurant.AlreadyExists", HttpStatus.BAD_REQUEST),
    RESTAURANT_UNAVAILABLE_FOR_DELIVERY("api.restaurant.noRestaurantAvailableForDelivery", HttpStatus.NOT_FOUND),
    RESTAURANT_ADDRESS_ALREADY_EXISTS("api.restaurant.addressAlreadyExists", HttpStatus.BAD_REQUEST),

    // Food
    FOOD_ALREADY_EXISTS("api.food.AlreadyExists", HttpStatus.BAD_REQUEST),
    FOOD_NOT_FOUND("api.food.notFound", HttpStatus.NOT_FOUND),
    ;

    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
