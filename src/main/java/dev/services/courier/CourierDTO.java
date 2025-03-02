package dev.services.courier;

import dev.core.validation.ValidEmail;
import dev.core.validation.ValidMobileNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nelson Tanko
 */
public class CourierDTO {
    private CourierDTO() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Courier name is required")
        private String name;

        @ValidMobileNumber
        private String phoneNumber;

        @ValidEmail
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String phoneNumber;
        private String email;
        private boolean available;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String name;

        @ValidMobileNumber
        private String phoneNumber;

        @ValidEmail
        private String email;

        private boolean available;
    }
}