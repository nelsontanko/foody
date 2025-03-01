package dev.account.web.vm;

import dev.core.validation.ValidMobileNumber;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Nelson Tanko
 */
@Data
public class UserUpdateVM {

    @Size(max = 50)
    private String fullname;

    @ValidMobileNumber
    private String mobileNumber;
}