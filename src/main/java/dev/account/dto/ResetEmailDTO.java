package dev.account.dto;

import dev.core.validation.ValidEmail;

/**
 * @author Nelson Tanko
 */
public record ResetEmailDTO(@ValidEmail String email) {
}
