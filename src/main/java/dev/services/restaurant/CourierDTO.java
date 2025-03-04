package dev.services.restaurant;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * @author Nelson Tanko
 */
public class CourierDTO {
    private CourierDTO() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Courier name is required")
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private boolean available;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String name;
        private boolean available;
    }
}