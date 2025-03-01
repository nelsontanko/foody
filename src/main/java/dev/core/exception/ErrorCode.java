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

    // Seller
    SELLER_NOT_FOUND("api.seller.notFound", HttpStatus.NOT_FOUND),
    SELLER_STATUS_ALREADY_ACTIVE("api.seller.statusAlreadyActive", HttpStatus.CONFLICT),
    SELLER_ID_REQUIRED_FOR_ADMIN("api.seller.idRequiredForAdmin", HttpStatus.BAD_REQUEST),
    SELLER_NOT_VERIFIED("api.seller.notVerified", HttpStatus.BAD_REQUEST),


    // Category
    CATEGORY_ALREADY_EXISTS("api.category.categoryAlreadyExists", HttpStatus.CONFLICT),
    CATEGORY_ID_REQUIRED("api.category.categoryIdRequired", HttpStatus.BAD_REQUEST),
    CATEGORY_NAME_NOT_FOUND("api.category.categoryNameNotFound", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_REQUIRED("api.category.categoryNameRequired", HttpStatus.BAD_REQUEST),

    // Admins
    ONLY_ADMINS_PROVIDE_IDS("api.admin.onlyAdminsRequireId", HttpStatus.CONFLICT),

    // Product
    PRODUCT_NOT_FOUND("api.product.productNotFound", HttpStatus.NOT_FOUND),
    PRODUCT_ONE_OR_MORE_NOT_FOUND("api.product.oneOrMoreNotFound", HttpStatus.NOT_FOUND),
    PRODUCT_INVALID_PRICES("MRP Price and Selling Price must be greater than zero.", HttpStatus.BAD_REQUEST),
    PRODUCT_STOCK_EXCEEDED("api.product.outOfStock", HttpStatus.CONFLICT),
    PRODUCT_INVALID_QUANTITY("api.product.invalidQuantity", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_IN_WISHLIST("api.product.notInWishlist", HttpStatus.CONFLICT),

    // Cart
    CART_ITEM_ERROR("api.cartItem.failToAdd", HttpStatus.CONFLICT),
    CART_NOT_FOUND("api.cart.notFound", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND("api.cartItem.notFound", HttpStatus.NOT_FOUND),
    CART_QUANTITY_MUST_BE_POSITIVE("api.cart.quantityMustBePositive", HttpStatus.BAD_REQUEST),
    CART_IS_EMPTY("api.cart.cartIsEmpty", HttpStatus.BAD_REQUEST),

    // Oder
    ORDER_NOT_FOUND("api.order.notFound", HttpStatus.NOT_FOUND),
    ORDER_CANNOT_BE_CANCELED("api.order.cannotBeCanceled", HttpStatus.CONFLICT),

    // Wishlist
    WISHLIST_NAME_ALREADY_EXISTS("api.wishlist.nameAlreadyExists", HttpStatus.CONFLICT),
    WISHLIST_NOT_FOUND("api.wishlist.notFound", HttpStatus.NOT_FOUND),
    WISHLIST_NOT_PUBLIC("api.wishlist.notPublic", HttpStatus.CONFLICT),

    // Review
    REVIEW_ALREADY_REVIEWED_BY_USER("api.review.alreadyReviewedByUser", HttpStatus.CONFLICT),
    REVIEW_NOT_FOUND("api.review.notFound", HttpStatus.NOT_FOUND),

    // Coupon
    COUPON_ALREADY_EXISTS("api.coupon.alreadyExists", HttpStatus.CONFLICT),
    COUPON_USAGE_LIMIT_REACHED("api.coupon.usageLimitReached", HttpStatus.BAD_REQUEST),
    COUPON_EXPIRED("api.coupon.expired", HttpStatus.BAD_REQUEST),
    COUPON_MINIMUM_ORDER_NOT_MET("api.coupon.minimumOrderNotMet", HttpStatus.BAD_REQUEST),
    COUPON_NOT_FOUND("api.coupon.notFound", HttpStatus.NOT_FOUND),
    COUPON_ALREADY_USED("api.coupon.alreadyUsed", HttpStatus.BAD_REQUEST),
    COUPON_INVALID_DISCOUNT_AMOUNT("api.coupon.invalidDiscountAmount", HttpStatus.BAD_REQUEST),
    COUPON_INVALID_MINIMUM_ORDER_AMOUNT("api.coupon.invalidMinimumOrderAmount", HttpStatus.BAD_REQUEST),
    COUPON_INVALID_DATE("api.coupon.invalidDate", HttpStatus.BAD_REQUEST),
    COUPON_INVALID_DATE_RANGE("api.coupon.invalidDateRange", HttpStatus.BAD_REQUEST),
    COUPON_NOT_FOR_USER("api.coupon.invalidForUser", HttpStatus.BAD_REQUEST),

    // Deal
    DEAL_NOT_FOUND("api.deal.notFound", HttpStatus.NOT_FOUND),
    DEAL_CANNOT_CANCEL_INACTIVE_DEAL("api.deal.cannotCancelInactiveDeal", HttpStatus.BAD_REQUEST),
    ;


    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
