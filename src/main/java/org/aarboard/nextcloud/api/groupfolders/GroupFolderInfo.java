package org.aarboard.nextcloud.api.groupfolders;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.io.FileUtils;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupFolderInfo {
    @JsonProperty
    private Integer id;
    @JsonProperty
    private String mount_point;
    @JsonProperty
    private Map<String, Integer> groups;
    @JsonProperty
    private Long quota;

    @JsonIgnore
    public Integer getId() {
        return id;
    }

    @JsonIgnore
    public String getMountPoint() {
        return mount_point;
    }

    @JsonIgnore
    public Map<String, Integer> getAssignedGroups() {
        return groups;
    }

    @JsonIgnore
    public Long getQuota() {
        if (quota == null)
            return null;
        return quota / FileUtils.ONE_GB;
    }
}
