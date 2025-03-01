package dev.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.core.Ordered;

/**
 * @author Nelson Tanko
 */
public class FoodyOpenApiCustomizer implements OpenApiCustomizer, Ordered {
    public static final int DEFAULT_ORDER = 0;
    private int order = 0;
    private final FoodyProperties.ApiDocs properties;

    public FoodyOpenApiCustomizer(FoodyProperties.ApiDocs properties) {
        this.properties = properties;
    }

    public void customise(OpenAPI openAPI) {
        Contact contact = (new Contact()).name(this.properties.getContactName()).url(this.properties.getContactUrl()).email(this.properties.getContactEmail());
        openAPI.info((new Info()).contact(contact).title(this.properties.getTitle()).description(this.properties.getDescription()).version(this.properties.getVersion()).termsOfService(this.properties.getTermsOfServiceUrl()).license((new License()).name(this.properties.getLicense()).url(this.properties.getLicenseUrl())));
        FoodyProperties.ApiDocs.Server[] var3 = this.properties.getServers();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            FoodyProperties.ApiDocs.Server server = var3[var5];
            openAPI.addServersItem((new Server()).url(server.getUrl()).description(server.getDescription()));
        }

    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }
}