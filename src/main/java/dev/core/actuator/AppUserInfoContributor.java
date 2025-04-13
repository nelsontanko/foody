package dev.core.actuator;

import dev.account.user.UserAccountRepository;
import lombok.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Nelson Tanko
 */
@Component
@Value
public class AppUserInfoContributor implements InfoContributor {
    UserAccountRepository accountRepository;

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("app-user.stats", Map.of("count", accountRepository.count())).build();
    }
}
