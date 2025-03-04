package dev.account;

import dev.account.user.Authority;

import java.util.UUID;

public class AuthorityTestSamples {

    public static Authority getAuthoritySample1() {
        return new Authority().name("name1");
    }

    public static Authority getAuthoritySample2() {
        return new Authority().name("name2");
    }
}
