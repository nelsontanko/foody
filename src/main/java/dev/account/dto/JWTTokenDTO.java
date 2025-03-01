package dev.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Nelson Tanko
 */
public class JWTTokenDTO {

    private String token;

    public JWTTokenDTO(String token) {
        this.token = token;
    }

    @JsonProperty("access_token")
    public String getToken() {
        return token;
    }

    public void setIdToken(String token) {
        this.token = token;
    }
}
