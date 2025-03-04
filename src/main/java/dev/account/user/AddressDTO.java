package dev.account.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * @author Nelson Tanko
 */
public class AddressDTO {
    private AddressDTO() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Street is required")
        private String street;

        private String city;

        private String country;

        private Double latitude;

        private Double longitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String street;

        private String city;

        private String country;

        private Double latitude;

        private Double longitude;
    }

}
