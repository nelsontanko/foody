package dev.account.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.stream.Collectors;

import static dev.security.SecurityUtils.AUTHORITIES_KEY;
import static dev.security.SecurityUtils.JWT_ALGORITHM;

/**
 * @author Nelson Tanko
 */
@Service
public class AuthenticationService {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    @Value("${foody.security.authentication.jwt.token-validity-in-seconds:0}")
    private long tokenValidityInSeconds;

    @Value("${foody.security.authentication.jwt.token-validity-in-seconds-for-remember-me:0}")
    private long tokenValidityInSecondsForRememberMe;

    private final JwtEncoder jwtEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public AuthenticationService(JwtEncoder jwtEncoder, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.jwtEncoder = jwtEncoder;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    /**
     * Authenticates a user and creates a JWT token
     *
     * @param email the email
     * @param password the password
     * @param rememberMe whether to extend the token validity
     * @return JWT token
     */
    public String authenticate(String email, String password, boolean rememberMe) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new AuthenticationException("Username and password cannot be empty") {
            };
        }

        LOG.debug("Attempting authentication for user: {}", email);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        LOG.info("User authenticated successfully: {}", email);
        return createToken(authentication, rememberMe);
    }

    private String createToken(Authentication authentication, boolean rememberMe) {
        LOG.debug("Creating token for user: {}, rememberMe: {}", authentication.getName(), rememberMe);
        String authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(""));

        var now = Instant.now();
        Instant validity;
        if (rememberMe) {
            validity = now.plusSeconds(this.tokenValidityInSecondsForRememberMe);
        } else {
            validity = now.plusSeconds(this.tokenValidityInSeconds);
        }
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    }
}
