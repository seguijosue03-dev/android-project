package com.student.learncraft;

public class PPTInfo {
    private String fileName;
    private String uriString;
    private long uploadTime;

    public PPTInfo() {
        this.uploadTime = System.currentTimeMillis();
    }

    public PPTInfo(String fileName, String uriString) {
        this.fileName = fileName;
        this.uriString = uriString;
        this.uploadTime = System.currentTimeMillis();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUriString() {
        return uriString;
    }

    public void setUriString(String uriString) {
        this.uriString = uriString;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }
}