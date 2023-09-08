package org.aarboard.nextcloud.api.groupfolders;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.aarboard.nextcloud.api.utils.JsonAnswer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class GroupFoldersListAnswer extends JsonAnswer {
    @JsonProperty
    @JsonDeserialize(using = GroupListDeserializer.class,
            keyAs = Integer.class, contentAs = GroupFolderInfo.class)
    public HashMap<Integer, GroupFolderInfo> data;

    @JsonIgnore
    public Collection<GroupFolderInfo> getAllGroupFolders() {
        if (data != null) {
            return data.values();
        }
        return Collections.emptyList();
    }
}
