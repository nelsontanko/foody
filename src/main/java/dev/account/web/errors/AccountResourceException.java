package dev.account.web.errors;

public class AccountResourceException extends RuntimeException {
    public AccountResourceException(String message) {
        super(message);
    }
}
