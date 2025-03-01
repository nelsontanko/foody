package dev.account.dto;

/**
 * @author Nelson Tanko
 */

public record PasswordChangeDTO(String currentPassword, String newPassword) {
}
