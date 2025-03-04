package dev.account.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Nelson Tanko
 */
@Data
public class UserUpdateDTO {

    @Size(max = 50)
    private String fullname;
}