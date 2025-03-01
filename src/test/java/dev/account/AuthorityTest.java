package dev.account;

import dev.account.user.Authority;
import dev.core.util.TestUtils;
import org.junit.jupiter.api.Test;

import static dev.account.AuthorityTestSamples.getAuthoritySample1;
import static dev.account.AuthorityTestSamples.getAuthoritySample2;
import static org.assertj.core.api.Assertions.assertThat;

class AuthorityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtils.equalsVerifier(Authority.class);
        Authority authority1 = getAuthoritySample1();
        Authority authority2 = new Authority();
        assertThat(authority1).isNotEqualTo(authority2);

        authority2.setName(authority1.getName());
        assertThat(authority1).isEqualTo(authority2);

        authority2 = getAuthoritySample2();
        assertThat(authority1).isNotEqualTo(authority2);
    }

    @Test
    void hashCodeVerifier() {
        Authority authority = new Authority();
        assertThat(authority.hashCode()).isZero();

        Authority authority1 = getAuthoritySample1();
        authority.setName(authority1.getName());
        assertThat(authority).hasSameHashCodeAs(authority1);
    }
}
