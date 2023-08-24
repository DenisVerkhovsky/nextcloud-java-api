package org.aarboard.nextcloud.api.groupfolders;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.aarboard.nextcloud.api.utils.JsonAnswer;

public class GroupFolderAnswer extends JsonAnswer {
    @JsonProperty
    private Data data;

    @JsonIgnore
    public Integer getId() {
        if (data != null) {
            return data.id;
        }
        return null;
    }

    public static class Data {
        @JsonProperty
        public Integer id;
    }
}
