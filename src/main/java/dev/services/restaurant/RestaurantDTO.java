package dev.services.restaurant;

import dev.account.user.AddressDTO;
import dev.core.validation.ValidEmail;
import dev.core.validation.ValidMobileNumber;
import dev.services.courier.CourierDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @author Nelson Tanko
 */
public class RestaurantDTO {
    private RestaurantDTO() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Restaurant name is required")
        private String name;

        @ValidEmail
        private String email;

        @ValidMobileNumber
        private String phoneNumber;

        private AddressDTO.Request address;

        private CourierDTO.Request courier;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private AddressDTO.Response address;
        private String phoneNumber;
        private String email;
        private boolean available;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailedResponse {
        private Long id;
        private String name;
        private AddressDTO.Response address;
        private String phoneNumber;
        private String email;
        private boolean active;
        private boolean available;
        private LocalDateTime availableFrom;
        private CourierDTO.Response courier;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String name;

        @ValidEmail
        private String email;

        @ValidMobileNumber
        private String phoneNumber;

        private boolean available;
        private CourierDTO.UpdateRequest courier;
    }
}
