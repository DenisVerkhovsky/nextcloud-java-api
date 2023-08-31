package org.aarboard.nextcloud.api.groupfolders;

import org.aarboard.nextcloud.api.ServerConfig;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;
import org.aarboard.nextcloud.api.utils.ConnectorCommon;
import org.aarboard.nextcloud.api.utils.EmptyAnswerParser;
import org.aarboard.nextcloud.api.utils.JsonAnswerParser;
import org.aarboard.nextcloud.api.utils.JsonSerializer;
import org.aarboard.nextcloud.api.utils.NextcloudResponseHelper;
import org.aarboard.nextcloud.api.utils.XMLAnswer;
import org.aarboard.nextcloud.api.utils.XMLAnswerParser;
import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GroupFolders {

    private final static String GROUP_FOLDERS_ROOT = "apps/groupfolders/folders";

    private final ConnectorCommon connectorCommon;

    private final JsonSerializer jsonSerializer;

    public GroupFolders(ServerConfig serverConfig) {
        this.connectorCommon = new ConnectorCommon(serverConfig);
        this.jsonSerializer = new JsonSerializer();
    }

    public int createGroupFolder(String path) {
        return NextcloudResponseHelper.getAndCheckStatus(createGroupFolderAsync(path)).getId();
    }

    public CompletableFuture<GroupFolderAnswer> createGroupFolderAsync(String path) {
        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair("mountpoint", path));
        return connectorCommon.executePost(GROUP_FOLDERS_ROOT, postParams,
                JsonAnswerParser.getInstance(GroupFolderAnswer.class));
    }

    public void renameGroupFolder(int groupFolderId, String newPath) {
        NextcloudResponseHelper.getAndCheckStatus(renameGroupFolderAsync(groupFolderId, newPath));
    }

    public CompletableFuture<XMLAnswer> renameGroupFolderAsync(int groupFolderId, String newPath) {
        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair("mountpoint", newPath));

        String url = String.format("%s/%d/mountpoint", GROUP_FOLDERS_ROOT, groupFolderId);

        return connectorCommon.executePost(url, postParams,
                XMLAnswerParser.getInstance(XMLAnswer.class));
    }

    public void deleteGroupFolder(int groupFolderId) {
        NextcloudResponseHelper.getAndCheckStatus(deleteGroupFolderAsync(groupFolderId));
    }

    public CompletableFuture<XMLAnswer> deleteGroupFolderAsync(int groupFolderId) {
        return connectorCommon.executeDelete(GROUP_FOLDERS_ROOT, String.valueOf(groupFolderId),
                XMLAnswerParser.getInstance(XMLAnswer.class));
    }

    public void grantAccess(int groupFolderId, String group) {
        NextcloudResponseHelper.getAndCheckStatus(grantAccessAsync(groupFolderId, group));
    }

    public CompletableFuture<XMLAnswer> grantAccessAsync(int groupFolderId, String group) {
        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair("group", group));

        String url = String.format("%s/%d/groups", GROUP_FOLDERS_ROOT, groupFolderId);

        return connectorCommon.executePost(url, postParams,
                XMLAnswerParser.getInstance(XMLAnswer.class));
    }

    public void forbidAccess(int groupFolderId, String group) {
        NextcloudResponseHelper.getAndCheckStatus(forbidAccessAsync(groupFolderId, group));
    }

    public CompletableFuture<XMLAnswer> forbidAccessAsync(int groupFolderId, String group) {
        return connectorCommon.executeDelete(GROUP_FOLDERS_ROOT, String.format("%d/groups/%s", groupFolderId, group),
                XMLAnswerParser.getInstance(XMLAnswer.class));
    }

    public void editPermissions(int groupFolderId, int permissions, String group) {
        NextcloudResponseHelper.getAndCheckStatus(editPermissionsAsync(groupFolderId, permissions, group));
    }

    public CompletableFuture<XMLAnswer> editPermissionsAsync(int groupFolderId, int permissions, String group) {
        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair("permissions", String.valueOf(permissions)));

        String url = String.format("%s/%d/groups/%s", GROUP_FOLDERS_ROOT, groupFolderId, group);

        return connectorCommon.executePost(url, postParams,
                XMLAnswerParser.getInstance(XMLAnswer.class));
    }

    public void setQuota(int groupFolderId, int quota) {
        try {
            setQuotaAsync(groupFolderId, quota).get();
        } catch (Exception e) {
            throw new NextcloudApiException(e);
        }
    }

    public CompletableFuture<Void> setQuotaAsync(int groupFolderId, int quota) {
        String groupFolderQuotaPath = String.format("%s/%d/quota", GROUP_FOLDERS_ROOT, groupFolderId);
        List<NameValuePair> postParams = new ArrayList<>();
        postParams.add(new BasicNameValuePair("quota", String.valueOf(quota * FileUtils.ONE_GB)));

        return connectorCommon.executePost(groupFolderQuotaPath, postParams, EmptyAnswerParser.getInstance());
    }
}
