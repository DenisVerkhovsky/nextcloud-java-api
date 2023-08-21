package org.aarboard.nextcloud.api.filesharing;

public class SharePermissionsAttribute {
    public enum Scope {
        PERMISSIONS("permissions");

        private final String value;

        Scope(String value) {
            this.value = value;
        }

        public String getStringValue() {
            return value;
        }

    }
    public enum Key {
        DOWNLOAD("download");

        private final String value;

        Key(String value) {
            this.value = value;
        }

        public String getStringValue() {
            return value;
        }

    }

    private String scope;
    private String key;
    private boolean enabled;
    public SharePermissionsAttribute(Scope scope, Key key, boolean enabled) {
        this.scope = scope.getStringValue();
        this.key = key.getStringValue();
        this.enabled = enabled;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}