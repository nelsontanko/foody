package dev.services.restaurant;

import dev.account.user.AddressDTO;
import dev.core.validation.ValidEmail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @author Nelson Tanko
 */
public class RestaurantDTO {
    private RestaurantDTO() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Restaurant name is required")
        private String name;

        @ValidEmail
        private String email;

        private String phoneNumber;

        @Valid
        @NotNull(message = "Courier is required")
        private CourierDTO.Request courier;

        @Valid
        @NotNull(message = "Address is required")
        private AddressDTO.Request address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String phoneNumber;
        private String email;
        private boolean active;
        private boolean available;
        private LocalDateTime availableFrom;
        private AddressDTO.Response address;
        private CourierDTO.Response courier;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String name;

        @ValidEmail
        private String email;

        private String phoneNumber;
        private Boolean available;
        private CourierDTO.UpdateRequest courier;
    }
}
