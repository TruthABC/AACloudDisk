package hk.hku.cs.aaclouddisk.entity.musicplayer;

public class ResourceInfo {

    private String name;
    private String onlineUrl;
    private boolean isOffline;
    private String offlineUrl;
    private boolean isLost;

    public ResourceInfo(String name, String onlineUrl, boolean isOffline, String offlineUrl) {
        this.name = name;
        this.onlineUrl = onlineUrl;
        this.isOffline = isOffline;
        this.offlineUrl = offlineUrl;
        this.isLost = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOnlineUrl() {
        return onlineUrl;
    }

    public void setOnlineUrl(String onlineUrl) {
        this.onlineUrl = onlineUrl;
    }

    public boolean getIsOffline() {
        return isOffline;
    }

    public void setIsOffline(boolean offline) {
        isOffline = offline;
    }

    public String getOfflineUrl() {
        return offlineUrl;
    }

    public void setOfflineUrl(String offlineUrl) {
        this.offlineUrl = offlineUrl;
    }

    public boolean getIsLost() {
        return isLost;
    }

    public void setIsLost(boolean lost) {
        isLost = lost;
    }
}
