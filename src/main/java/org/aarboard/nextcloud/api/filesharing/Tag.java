package org.aarboard.nextcloud.api.filesharing;

public class Tag {
    private int id;
    private String name;
    private boolean canAssign;
    private boolean userAssignable;
    private boolean userVisible;

    public Tag(String name, boolean canAssign, boolean userAssignable, boolean userVisible) {
        this.name = name;
        this.canAssign = canAssign;
        this.userAssignable = userAssignable;
        this.userVisible = userVisible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCanAssign() {
        return canAssign;
    }

    public void setCanAssign(boolean canAssign) {
        this.canAssign = canAssign;
    }

    public boolean isUserAssignable() {
        return userAssignable;
    }

    public void setUserAssignable(boolean userAssignable) {
        this.userAssignable = userAssignable;
    }

    public boolean isUserVisible() {
        return userVisible;
    }

    public void setUserVisible(boolean userVisible) {
        this.userVisible = userVisible;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
