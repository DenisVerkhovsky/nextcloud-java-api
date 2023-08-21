package org.aarboard.nextcloud.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;

public class JsonSerializer {
    private final ObjectMapper objectMapper;

    public JsonSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    public <T> String toString(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new NextcloudApiException(e);
        }
    }
}
