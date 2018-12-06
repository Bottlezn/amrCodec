package cn.wzh.amrcodec.bean;

import java.io.Serializable;

/**
 * author: wangzh
 * create: 2018/11/30 11:02
 * description: 音频文件类
 * version: 1.0
 */
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fileType;

    private long fileSize;

    private int sampleRate;

    private int bitsPerSample;

    private int channels;

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getBitsPerSample() {
        return bitsPerSample;
    }

    public void setBitsPerSample(int bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                ", sampleRate=" + sampleRate +
                ", bitsPerSample=" + bitsPerSample +
                ", channels=" + channels +
                '}';
    }
}
