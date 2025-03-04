package dev.account.web.vm;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Nelson Tanko
 */
@Setter
@Getter
public class KeyAndPasswordVM {

    private String resetKey;

    private String newPassword;
}
