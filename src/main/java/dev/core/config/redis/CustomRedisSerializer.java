package dev.core.config.redis;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;

/**
 * Custom Redis serializer that handles Page objects by converting them to SerializablePage
 * before serialization and back after deserialization.
 */
public class CustomRedisSerializer implements RedisSerializer<Object> {

    private final ObjectMapper objectMapper;

    public CustomRedisSerializer() {
        this.objectMapper = new ObjectMapper();

        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        objectMapper.registerModule(new JavaTimeModule());

        SimpleModule springDataModule = new SimpleModule();
        objectMapper.registerModule(springDataModule);

        objectMapper.addMixIn(Page.class, PageMixin.class);
        objectMapper.addMixIn(Sort.class, SortMixin.class);
    }

    @Override
    public byte[] serialize(Object object) throws SerializationException {
        if (object == null) {
            return new byte[0];
        }

        try {
            if (object instanceof Page && !(object instanceof SerializablePage)) {
                SerializablePage<?> serializablePage = new SerializablePage<>((Page<?>) object);

                String json = objectMapper.writeValueAsString(serializablePage);

                ObjectNode node = (ObjectNode) objectMapper.readTree(json);

                if (node.has("sort")) {
                    node.remove("sort");
                }

                return objectMapper.writeValueAsBytes(node);
            }

            if (hasSortField(object)) {
                String json = objectMapper.writeValueAsString(object);

                ObjectNode node = (ObjectNode) objectMapper.readTree(json);

                if (node.has("sort")) {
                    node.remove("sort");
                }

                return objectMapper.writeValueAsBytes(node);
            }

            return objectMapper.writeValueAsBytes(object);
        } catch (IOException e) {
            throw new SerializationException("Error serializing object to JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(bytes);

            if (rootNode.has("@class") &&
                    rootNode.get("@class").asText().contains("SerializablePage")) {
                return objectMapper.readValue(bytes, SerializablePage.class);
            }

            return objectMapper.readValue(bytes, Object.class);
        } catch (Exception e) {
            throw new SerializationException("Error deserializing JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Check if the object has a sort field that might cause serialization issues
     */
    private boolean hasSortField(Object object) {
        try {
            // Use reflection to check for a sort field
            return object.getClass().getDeclaredField("sort") != null;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    interface PageMixin {
        // This is just a marker interface to apply @JsonIgnoreProperties
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    interface SortMixin {
        // This is just a marker interface to apply @JsonIgnoreProperties
    }
}