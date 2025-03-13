package dev.core.config.cache;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.data.domain.Page;

import java.io.IOException;

/**
 * @author Nelson Tanko
 */
@JsonComponent
public class PageSerializer extends JsonSerializer<Page<?>> {

    @Override
    public void serialize(Page<?> page, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        gen.writeFieldName("content");
        gen.writeStartArray();
        for (Object item : page.getContent()) {
            provider.defaultSerializeValue(item, gen);
        }
        gen.writeEndArray();

        gen.writeFieldName("page");
        gen.writeStartObject();
        gen.writeNumberField("size", page.getSize());
        gen.writeNumberField("number", page.getNumber());
        gen.writeNumberField("totalElements", page.getTotalElements());
        gen.writeNumberField("totalPages", page.getTotalPages());
        gen.writeEndObject();

        gen.writeEndObject();
    }
}
