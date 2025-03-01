package dev.account.web.vm;

import dev.core.validation.ValidEmail;

/**
 * @author Nelson Tanko
 */
public record ResetEmailVM(@ValidEmail String email) {
}
