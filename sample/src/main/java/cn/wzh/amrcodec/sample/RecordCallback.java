package cn.wzh.amrcodec.sample;

import java.io.File;

/**
 * author: wangzh
 * create: 2018/11/30 16:45
 * description: 录制wav音频的回调
 * version: 1.0
 */
public interface RecordCallback {

    void createFile(boolean result);

    void finished(File outputFile);

    void error(Throwable t);
}
