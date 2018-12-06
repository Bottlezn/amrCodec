package cn.wzh.amrcodec.sample;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.wzh.amrcodec.sample.consts.RecordConst;

import static android.media.AudioRecord.ERROR;
import static android.media.AudioRecord.ERROR_BAD_VALUE;
import static android.media.AudioRecord.ERROR_DEAD_OBJECT;
import static android.media.AudioRecord.ERROR_INVALID_OPERATION;

/**
 * author: wangzh
 * create: 2018/11/30 15:37
 * description: 可以暂停，重新开始录制pcm文件的管理类，并将pcm文件转换成wav文件
 * version: 1.0
 */
public class AudioRecordManager {

    private static final String TAG = AudioRecordManager.class.getSimpleName();

    private static final String SUFFIX_WAV = ".wav";
    private static final String SUFFIX_PCM = ".pcm";

    private static boolean isStop = false;
    private static boolean isCancel = false;

    /**
     * 声源用最高采样率
     */
    private static final int SAMPLE_RATE_IN_HZ = 44100;

    private static AudioRecordManager sInstance;

    public static AudioRecordManager getInstance() {
        if (sInstance == null) {
            synchronized (TAG) {
                if (null == sInstance) {
                    sInstance = new AudioRecordManager();
                }
            }
        }
        return sInstance;
    }

    private AudioRecord mRecord;

    private AudioRecordManager() {

    }

    /**
     * 没有带后缀的文件名
     */
    private static String outputFileNameWithoutSuffix;

    private void initRecord(@NonNull File outWavFile, @NonNull RecordCallback callback) {
        isStop = false;
        isCancel = false;
        //缓存size
        int minBufSize = 2 * AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mRecord = new AudioRecord(RecordConst.AUDIO_SOURCE,
                RecordConst.SAMPLE_RATE_IN_HZ,
                RecordConst.CHANNEL,
                RecordConst.AUDIO_FORMAT,
                minBufSize);
        Log.i(TAG, "minBufSize = " + minBufSize);
        mWriteThread = new WriterThread(outWavFile, callback);
        mWriteThread.start();
        mRecordThread = new RecordThread(mRecord, minBufSize);
        mRecordThread.start();
        mRecord.startRecording();
    }

    private RecordThread mRecordThread;
    private WriterThread mWriteThread;


    private void releaseSource() {
        isCancel = true;
        isStop = true;
        mWriteThread = null;
        mRecordThread = null;
        if (mRecord != null) {
            mRecord.stop();
            mRecord.release();
        }
        mRecord = null;
    }

    /**
     * 强制释放，会把线程interrupt掉
     */
    private void forceReleaseSource() {
        isCancel = true;
        isStop = true;
        if (mRecordThread != null) {
            mRecordThread.interrupt();
        }
        if (mWriteThread != null) {
            mWriteThread.interrupt();
        }
        mWriteThread = null;
        mRecordThread = null;
        if (mRecord != null) {
            mRecord.stop();
            mRecord.release();
        }
        mRecord = null;
    }

    public void cancel() {
        forceReleaseSource();
    }

    /**
     * 释放资源
     */
    public void stopRecord() {
        releaseSource();
    }

    /**
     * 开始录音，会保存成一个pcm格式的文件，懒猴再转换成wav格式的文件
     *
     * @param absolutePath 保存的wav文件的绝对路径，也可以不带后缀，有根目录和文件名即可
     * @param callback     保存的回调
     */
    public void startRecord(String absolutePath, RecordCallback callback) {
        releaseSource();
        initRecord(new File(handlePath(absolutePath)), callback);
    }

    private String handlePath(String absolutePath) {
        if (absolutePath.endsWith(SUFFIX_WAV)) {
            absolutePath = absolutePath.substring(0, absolutePath.length() - 4);
        }
        outputFileNameWithoutSuffix = absolutePath;
        absolutePath += SUFFIX_PCM;
        return absolutePath;
    }

    public void onPause() {
        mRecord.stop();
        isStop = true;
    }

    public void onResume() {
        mRecord.startRecording();
        isStop = false;
    }

    private static class RecordThread extends Thread {

        private AudioRecord mAudioRecord;

        private int minBufSize;

        private RecordThread(AudioRecord record, int minBufSize) {
            this.mAudioRecord = record;
            this.minBufSize = minBufSize;
        }

        @Override
        public void run() {
            super.run();
            byte[] buffer = new byte[minBufSize];
            while (!isCancel) {
                while (!isStop) {
                    int len = mAudioRecord.read(buffer, 0, buffer.length);
                    if (!isSuccess(len)) {
                        Log.e(TAG, "读取失败 ，len = " + len);
                        continue;
                    }
                    SAFE_LIST.add(new Data(buffer.clone(), len));
                }
            }
        }

    }

    private static final CopyOnWriteArrayList<Data> SAFE_LIST = new CopyOnWriteArrayList<>();

    private static class Data {
        private byte[] buff;
        private int length;

        public Data(byte[] buff, int length) {
            this.buff = buff;
            this.length = length;
        }
    }

    private static class WriterThread extends Thread {

        private File mFile;
        private FileOutputStream mFos;
        private RecordCallback mCallback;

        WriterThread(File file, RecordCallback callback) {
            mFile = file;
            this.mCallback = callback;
        }

        @Override
        public void run() {
            super.run();
            try {
                mFos = new FileOutputStream(mFile);
                MainHandler.getInstance().postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.createFile(true);
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                MainHandler.getInstance().postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.createFile(false);
                    }
                });
                return;
            }
            while (!isCancel || !SAFE_LIST.isEmpty()) {
                if (!SAFE_LIST.isEmpty()) {
                    Data data = SAFE_LIST.remove(0);
                    try {
                        writeFile(data.buff, data.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (mCallback != null) {
                MainHandler.getInstance().postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.finished(mFile);
                    }
                });
            }
        }

        /**
         * 写入时候会调节录音音量
         *
         * @param buff   buff
         * @param length 长度
         * @throws IOException 异常
         */
        private void writeFile(byte[] buff, int length) throws IOException {
            for (int i = 0; i < length; i++) {
                //音量放大，以后优化放大算法.现在默认2倍
                buff[i] = (byte) (buff[i] * 2);
            }
            this.mFos.write(buff, 0, length);
            this.mFos.flush();
        }
    }

    @SuppressLint("InlinedApi")
    private static boolean isSuccess(int len) {
        return len != ERROR_INVALID_OPERATION &&
                len != ERROR_BAD_VALUE &&
                len != ERROR_DEAD_OBJECT &&
                len != ERROR;
    }

}
