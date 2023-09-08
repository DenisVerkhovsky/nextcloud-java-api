package org.aarboard.nextcloud.api.groupfolders;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GroupListDeserializer extends JsonDeserializer<Map<Integer, GroupFolderInfo>> {

    @Override
    public Map<Integer, GroupFolderInfo> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        return this.deserialize(p, new HashMap<>());
    }

    private Map<Integer, GroupFolderInfo> deserialize(JsonParser p,
            Map<Integer, GroupFolderInfo> intoValue) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

        JsonNode node = mapper.readTree(p);

        if (node.isContainerNode()) {
            node.forEach(entry -> {
                try {
                    GroupFolderInfo info = mapper.readValue(entry.toString(), GroupFolderInfo.class);
                    intoValue.put(info.getId(), info);
                } catch (NullPointerException | IOException e) {
                    throw new NextcloudApiException(e);
                }
            });
        }
        return intoValue;
    }
}
