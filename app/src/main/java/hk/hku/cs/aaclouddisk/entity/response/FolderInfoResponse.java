package hk.hku.cs.aaclouddisk.entity.response;

import java.util.ArrayList;

public class FolderInfoResponse extends CommonResponse {

    private ArrayList<FileInfo> fileInfoList;

    public FolderInfoResponse() {}

    public FolderInfoResponse(ArrayList<FileInfo> fileInfoList) {
        super();
        this.fileInfoList = fileInfoList;
    }

    public FolderInfoResponse(int errcode, String errmsg, ArrayList<FileInfo> fileInfoList) {
        super(errcode, errmsg);
        this.fileInfoList = fileInfoList;
    }

    public ArrayList<FileInfo> getFileInfoList() {
        return fileInfoList;
    }

    public void setFileInfoList(ArrayList<FileInfo> fileInfoList) {
        this.fileInfoList = fileInfoList;
    }
}
