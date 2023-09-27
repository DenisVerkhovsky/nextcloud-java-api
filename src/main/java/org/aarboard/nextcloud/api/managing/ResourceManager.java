package org.aarboard.nextcloud.api.managing;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.CompletableFuture;

import org.aarboard.nextcloud.api.ServerConfig;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;
import org.aarboard.nextcloud.api.filesharing.Tag;
import org.aarboard.nextcloud.api.utils.ConnectorCommon;
import org.aarboard.nextcloud.api.utils.EmptyAnswerParser;
import org.aarboard.nextcloud.api.utils.JsonSerializer;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class ResourceManager implements AutoCloseable {
    private final static String SYSTEM_TAGS = "/remote.php/dav/systemtags";
    private final static String SYSTEM_TAGS_RELATIONS = "/remote.php/dav/systemtags-relations/files";

    private final ConnectorCommon connectorCommon;

    private final JsonSerializer jsonSerializer;

    public ResourceManager(ServerConfig serverConfig) {
        this.connectorCommon = new ConnectorCommon(serverConfig);
        this.jsonSerializer = new JsonSerializer();
    }

    public void setTag(int sourceId, int tagId) {
        try {
            setTagAsync(sourceId, tagId).get();
        } catch (Exception e) {
            throw new NextcloudApiException(e);
        }
    }

    public CompletableFuture<Void> setTagAsync(int sourceId, int tagId) {
        return connectorCommon.executePut(SYSTEM_TAGS_RELATIONS, sourceId + "/" + tagId, null,
                EmptyAnswerParser.getInstance());
    }

    public void removeTag(int sourceId, int tagId) {
        try {
            removeTagAsync(sourceId, tagId).get();
        } catch (Exception e) {
            throw new NextcloudApiException(e);
        }
    }

    public CompletableFuture<Void> removeTagAsync(int sourceId, int tagId) {
        return connectorCommon.executeDelete(SYSTEM_TAGS_RELATIONS, sourceId + "/" + tagId, null,
                EmptyAnswerParser.getInstance());
    }

    public void createTag(Tag tag) {
        try {
            createTagAsync(tag).get();
        } catch (Exception e) {
            throw new NextcloudApiException(e);
        }
    }

    public CompletableFuture<Void> createTagAsync(Tag tag) {
        try {
            HttpEntity httpEntity = new StringEntity(jsonSerializer.toString(tag),
                    ContentType.APPLICATION_JSON);
            return connectorCommon.executePost(SYSTEM_TAGS, null, null, httpEntity,
                    EmptyAnswerParser.getInstance());
        } catch (UnsupportedCharsetException e) {
            throw new NextcloudApiException(e);
        }
    }

    @Override
    public void close() throws IOException {
        connectorCommon.close();
    }
}
