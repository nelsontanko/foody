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
    USER_PROFILE_INVALID_PHONE("api.user.invalidPhoneNumber", HttpStatus.BAD_REQUEST),
    USER_NOT_LOGGED_IN("api.user.notLoggedIn", HttpStatus.BAD_REQUEST),

    // Admins
    ONLY_ADMINS_PROVIDE_IDS("api.admin.onlyAdminsRequireId", HttpStatus.CONFLICT),

    // Oder
    ORDER_NOT_FOUND("api.order.notFound", HttpStatus.NOT_FOUND),
    ORDER_CANNOT_BE_CANCELED("api.order.cannotBeCanceled", HttpStatus.CONFLICT),

    // Review
    REVIEW_ALREADY_REVIEWED_BY_USER("api.review.alreadyReviewedByUser", HttpStatus.CONFLICT),
    REVIEW_NOT_FOUND("api.review.notFound", HttpStatus.NOT_FOUND),

    // Restaurant
    RESTAURANT_NOT_FOUND("api.restaurant.notFound", HttpStatus.NOT_FOUND),
    RESTAURANT_ALREADY_EXISTS("api.restaurant.AlreadyExists", HttpStatus.BAD_REQUEST),;


    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
