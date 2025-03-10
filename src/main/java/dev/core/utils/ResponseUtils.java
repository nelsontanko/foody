package dev.core.utils;

import dev.core.dto.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author Nelson Tanko
 */
public interface ResponseUtils {

    /**
     * Wrap the optional into a {@link ResponseEntity} with an {@link HttpStatus#OK} status, or if it's empty, it
     * returns a {@link ResponseEntity} with {@link HttpStatus#NOT_FOUND}.
     *
     * @param <X>           type of the response
     * @param maybeResponse response to return if present
     * @return response containing {@code maybeResponse} if present or {@link HttpStatus#NOT_FOUND}
     */
    static <X> ResponseEntity<X> wrapOrNotFound(Optional<X> maybeResponse) {
        return wrapOrNotFound(maybeResponse, null);
    }

    /**
     * Wrap the optional into a {@link ResponseEntity} with an {@link HttpStatus#OK} status with the headers, or if it's
     * empty, throws a {@link ResponseStatusException} with status {@link HttpStatus#NOT_FOUND}.
     *
     * @param <X>           type of the response
     * @param maybeResponse response to return if present
     * @param header        headers to be added to the response
     * @return response containing {@code maybeResponse} if present
     */
    static <X> ResponseEntity<X> wrapOrNotFound(Optional<X> maybeResponse, HttpHeaders header) {
        return maybeResponse
                .map(response -> ResponseEntity.ok().headers(header).body(response))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    static <T> ResponseEntity<ApiResponse<T>> createSuccessResponse(
            T data, String message, HttpStatus status) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .status(status.value())
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, status);
    }
}
