package dev.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Nelson Tanko
 */
@Data
@AllArgsConstructor
public class JWTTokenDTO {

    private String message;
    private String token;

    @JsonProperty("access_token")
    public String getToken() {
        return token;
    }

    public void setIdToken(String token) {
        this.token = token;
    }
}
