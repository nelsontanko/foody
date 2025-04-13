package dev.core.actuator;

import dev.account.user.AuthorityRepository;
import lombok.Value;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Nelson Tanko
 */
@Component
@Value
@Endpoint(id = "app-authorities")
public class AuthoritiesEndpoint {

    AuthorityRepository authorityRepository;

    @ReadOperation
    Map<String, Long> count(){
        return Map.of("count", authorityRepository.count());
    }
}
