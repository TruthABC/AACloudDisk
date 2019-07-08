package hk.hku.cs.aaclouddisk.entity.musicplayer;

import java.util.List;

public class MusicList {

    private String userId;
    private String listName;
    private boolean isUserCreated;
    private long lastRevised;
    private List<ResourceInfo> resourceList;
    private boolean isAllOffline;

    public MusicList(String userId, String listName, boolean isUserCreated, long lastRevised, List<ResourceInfo> resourceList, boolean isAllOffline) {
        this.userId = userId;
        this.listName = listName;
        this.isUserCreated = isUserCreated;
        this.lastRevised = lastRevised;
        this.resourceList = resourceList;
        this.isAllOffline = isAllOffline;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public boolean getIsUserCreated() {
        return isUserCreated;
    }

    public void setIsUserCreated(boolean isUserCreated) {
        this.isUserCreated = isUserCreated;
    }

    public long getLastRevised() {
        return lastRevised;
    }

    public void setLastRevised(long lastRevised) {
        this.lastRevised = lastRevised;
    }

    public List<ResourceInfo> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<ResourceInfo> resourceList) {
        this.resourceList = resourceList;
    }

    public boolean getIsAllOffline() {
        return isAllOffline;
    }

    public void setIsAllOffline(boolean allOffline) {
        isAllOffline = allOffline;
    }
}
