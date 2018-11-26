package hk.hku.cs.aaclouddisk.entity.response;

public class CommonResponse {
    private int errcode;
    private String errmsg;

    public CommonResponse() {
        this.errcode = 0;
        this.errmsg = "";
    }

    public CommonResponse(int errcode, String errmsg) {
        this.errcode = errcode;
        this.errmsg = errmsg;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
}
