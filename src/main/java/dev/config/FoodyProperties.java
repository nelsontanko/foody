package dev.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Nelson Tanko
 */
@Getter
@ConfigurationProperties(
        prefix = "foody",
        ignoreUnknownFields = false
)
public class FoodyProperties {

    private final CorsConfiguration cors = new CorsConfiguration();
    private final ClientApp clientApp = new ClientApp();
    private final Cache cache = new Cache();
    private final Logging logging = new Logging();
    private final Security security = new Security();
    private final Async async = new Async();
    private final ApiDocs apiDocs = new ApiDocs();

    public FoodyProperties() {
    }

    @Getter @Setter
    public static class Cache {
        private Caffeine caffeine = new Caffeine();

        public Cache() {
        }

        @Getter @Setter
        public static class Caffeine {
            private int timeToLiveSeconds = FoodyDefaults.Cache.Caffeine.timeToLiveSeconds;
            private long maxEntries = FoodyDefaults.Cache.Caffeine.maxEntries;

            public Caffeine() {
            }
        }
    }

    @Getter
    public static class Logging {
        private boolean useJsonFormat = false;
        private final Logstash logstash = new Logstash();


        public Logging() {
        }

        public boolean isUseJsonFormat() {
            return this.useJsonFormat;
        }

        public void setUseJsonFormat(boolean useJsonFormat) {
            this.useJsonFormat = useJsonFormat;
        }

        public Logstash getLogstash() {
            return this.logstash;
        }

        @Setter @Getter
        public static class Logstash {
            private boolean enabled = false;
            private String host = "localhost";
            private int port = 5000;
            private int ringBufferSize = 512;

            public Logstash() {
            }
        }
    }

    public static class Security {
        private String contentSecurityPolicy = "default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:";
        private final ClientAuthorization clientAuthorization = new ClientAuthorization();
        private final Authentication authentication = new Authentication();
        private final RememberMe rememberMe = new RememberMe();
        private final OAuth2 oauth2 = new OAuth2();

        public Security() {
        }

        public ClientAuthorization getClientAuthorization() {
            return this.clientAuthorization;
        }

        public Authentication getAuthentication() {
            return this.authentication;
        }

        public RememberMe getRememberMe() {
            return this.rememberMe;
        }

        public OAuth2 getOauth2() {
            return this.oauth2;
        }

        public String getContentSecurityPolicy() {
            return this.contentSecurityPolicy;
        }

        public void setContentSecurityPolicy(String contentSecurityPolicy) {
            this.contentSecurityPolicy = contentSecurityPolicy;
        }

        public static class ClientAuthorization {
            private String accessTokenUri;
            private String tokenServiceId;
            private String clientId;
            private String clientSecret;

            public ClientAuthorization() {
                this.accessTokenUri = FoodyDefaults.Security.ClientAuthorization.accessTokenUri;
                this.tokenServiceId = FoodyDefaults.Security.ClientAuthorization.tokenServiceId;
                this.clientId = FoodyDefaults.Security.ClientAuthorization.clientId;
                this.clientSecret = FoodyDefaults.Security.ClientAuthorization.clientSecret;
            }

            public String getAccessTokenUri() {
                return this.accessTokenUri;
            }

            public void setAccessTokenUri(String accessTokenUri) {
                this.accessTokenUri = accessTokenUri;
            }

            public String getTokenServiceId() {
                return this.tokenServiceId;
            }

            public void setTokenServiceId(String tokenServiceId) {
                this.tokenServiceId = tokenServiceId;
            }

            public String getClientId() {
                return this.clientId;
            }

            public void setClientId(String clientId) {
                this.clientId = clientId;
            }

            public String getClientSecret() {
                return this.clientSecret;
            }

            public void setClientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
            }
        }

        public static class Authentication {
            private final Jwt jwt = new Jwt();

            public Authentication() {
            }

            public Jwt getJwt() {
                return this.jwt;
            }

            public static class Jwt {
                private String secret;
                private String base64Secret;
                private long tokenValidityInSeconds;
                private long tokenValidityInSecondsForRememberMe;

                public Jwt() {
                    this.secret = FoodyDefaults.Security.Authentication.Jwt.secret;
                    this.base64Secret = FoodyDefaults.Security.Authentication.Jwt.base64Secret;
                    this.tokenValidityInSeconds = 1800L;
                    this.tokenValidityInSecondsForRememberMe = 2592000L;
                }

                public String getSecret() {
                    return this.secret;
                }

                public void setSecret(String secret) {
                    this.secret = secret;
                }

                public String getBase64Secret() {
                    return this.base64Secret;
                }

                public void setBase64Secret(String base64Secret) {
                    this.base64Secret = base64Secret;
                }

                public long getTokenValidityInSeconds() {
                    return this.tokenValidityInSeconds;
                }

                public void setTokenValidityInSeconds(long tokenValidityInSeconds) {
                    this.tokenValidityInSeconds = tokenValidityInSeconds;
                }

                public long getTokenValidityInSecondsForRememberMe() {
                    return this.tokenValidityInSecondsForRememberMe;
                }

                public void setTokenValidityInSecondsForRememberMe(long tokenValidityInSecondsForRememberMe) {
                    this.tokenValidityInSecondsForRememberMe = tokenValidityInSecondsForRememberMe;
                }
            }
        }

        public static class RememberMe {
            private @NotNull String key;

            public RememberMe() {
                this.key = FoodyDefaults.Security.RememberMe.key;
            }

            public String getKey() {
                return this.key;
            }

            public void setKey(String key) {
                this.key = key;
            }
        }

        public static class OAuth2 {
            private List<String> audience = new ArrayList();

            public OAuth2() {
            }

            public List<String> getAudience() {
                return Collections.unmodifiableList(this.audience);
            }

            public void setAudience(@NotNull List<String> audience) {
                this.audience.addAll(audience);
            }
        }
    }


    public static class ClientApp {
        private String name = "foodyApp";

        public ClientApp() {
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Getter @Setter
    public static class Async {
        private int corePoolSize = 2;
        private int maxPoolSize = 50;
        private int queueCapacity = 10000;

        public Async() {
        }
    }

    @Setter @Getter
    public static class ApiDocs {
        private String title = "Foody Application API";
        private String description = "API documentation";
        private String version = "1.0";
        private String termsOfServiceUrl;
        private String contactName;
        private String contactUrl;
        private String contactEmail;
        private String license;
        private String licenseUrl;
        private String[] defaultIncludePattern;
        private String[] managementIncludePattern;
        private Server[] servers;

        public ApiDocs() {
            this.termsOfServiceUrl = FoodyDefaults.ApiDocs.termsOfServiceUrl;
            this.contactName = FoodyDefaults.ApiDocs.contactName;
            this.contactUrl = FoodyDefaults.ApiDocs.contactUrl;
            this.contactEmail = FoodyDefaults.ApiDocs.contactEmail;
            this.license = FoodyDefaults.ApiDocs.license;
            this.licenseUrl = FoodyDefaults.ApiDocs.licenseUrl;
            this.defaultIncludePattern = FoodyDefaults.ApiDocs.defaultIncludePattern;
            this.managementIncludePattern = FoodyDefaults.ApiDocs.managementIncludePattern;
            this.servers = new Server[0];
        }

        public static class Server {
            private String url;
            private String description;

            public Server() {
            }

            public String getUrl() {
                return this.url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getDescription() {
                return this.description;
            }

            public void setDescription(String description) {
                this.description = description;
            }
        }
    }
}
