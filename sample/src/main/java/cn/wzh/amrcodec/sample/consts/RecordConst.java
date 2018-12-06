package cn.wzh.amrcodec.sample.consts;

import android.media.AudioFormat;
import android.media.MediaRecorder;

/**
 * author: wangzh
 * create: 2018/12/1 11:20
 * description: 录音用到的常量
 * version: 1.0
 */
public interface RecordConst {

    int SAMPLE_RATE_IN_HZ = 8000;

    int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    int CHANNEL = AudioFormat.CHANNEL_IN_DEFAULT;

    int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

}
