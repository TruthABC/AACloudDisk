package hk.hku.cs.aaclouddisk.entity.response;

public class FileInfo {

    private String name;
    private String relativePath;
    private int dir;

    public FileInfo() {}

    public FileInfo(String name, String relativePath, int dir) {
        this.name = name;
        this.relativePath = relativePath;
        this.dir = dir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public int getDir() {
        return dir;
    }

    public void setDir(int dir) {
        this.dir = dir;
    }
}
