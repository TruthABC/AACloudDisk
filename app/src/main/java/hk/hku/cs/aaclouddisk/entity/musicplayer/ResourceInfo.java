package hk.hku.cs.aaclouddisk.entity.musicplayer;

public class ResourceInfo {

    private String name;
    private String onlineUrl;
    private boolean isOffline;
    private String offlineUrl;

    public ResourceInfo(String name, String onlineUrl, boolean isOffline, String offlineUrl) {
        this.name = name;
        this.onlineUrl = onlineUrl;
        this.isOffline = isOffline;
        this.offlineUrl = offlineUrl;
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

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    public String getOfflineUrl() {
        return offlineUrl;
    }

    public void setOfflineUrl(String offlineUrl) {
        this.offlineUrl = offlineUrl;
    }
}
