package hk.hku.cs.aaclouddisk.entity.response;

public class GreetingResponse {

    private final long id;
    private final String content;

    public GreetingResponse(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}