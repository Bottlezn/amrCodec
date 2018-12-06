package cn.wzh.amrcodec.utils;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.wzh.amrcodec.bean.FileInfo;
import cn.wzh.amrcodec.encoder.AmrMode;
import cn.wzh.amrcodec.encoder.AmrEncoder;
import cn.wzh.amrcodec.exception.InvalidWaveException;
import cn.wzh.amrcodec.io.WaveReader;

/**
 * author: wangzh
 * create: 2018/11/30 11:29
 * description: 将Wav文件转换成amr文件
 * version: 1.0
 */
public class Wav2Amr {

    private static final String TAG = Wav2Amr.class.getSimpleName();
    // AMR_HEADER，6个长度的头头头
    private final static byte[] AMR_HEADER = new byte[]{'#', '!', 'A', 'M', 'R', '\n'};


    public Wav2Amr() {

    }

    // amr-nb encoder
    public FileInfo[] convertwav(File input, File output, AmrMode reqMode, int sampleRateInHz)
            throws InvalidWaveException, IOException {
        FileInfo[] finfor = new FileInfo[2];
        FileInfo wavFile = new FileInfo();
        FileInfo amrFile = new FileInfo();
        finfor[0] = wavFile;
        finfor[1] = amrFile;
        // 8khz 8000*0.02=160，16kHz 16000*0.02=320 ，44.1kHz 44100*0.02 =882
        final int PCM_FRAME_SIZE = (int) (sampleRateInHz * 0.02f);

        long fileSize = input.length();
        Log.i(TAG, "fileSize = " + fileSize);
        wavFile.setFileType("wav");
        wavFile.setFileSize(fileSize);
        //缓存的大小
        int OUTPUT_STREAM_BUFFER = PCM_FRAME_SIZE * 4;
        /* File output stream */
        BufferedOutputStream mOutStream;
        FileOutputStream fileStream = new FileOutputStream(output);
        mOutStream = new BufferedOutputStream(fileStream, OUTPUT_STREAM_BUFFER);

        WaveReader wav = new WaveReader(input);
        wav.openWave();
        wavFile.setSampleRate(wav.getSampleRate());
        wavFile.setBitsPerSample(wav.getPcmFormat());
        wavFile.setChannels(wav.getChannels());
        Log.i(TAG + "WTF", wav.toString());
        amrFile.setFileType("amr");
        amrFile.setSampleRate(8000);
        amrFile.setBitsPerSample(8);
        amrFile.setChannels(wav.getChannels());
        int dtx = 0;
        // the pointer to the native
        int mNativeAmrEncoder = AmrEncoder.initEncAmr(dtx);
        // write the AMR_HEADER
        mOutStream.write(AMR_HEADER, 0, 6);
        int bytecount = 0;
        while (true) {
            short[] speech = new short[PCM_FRAME_SIZE];
            byte[] outbuf = new byte[PCM_FRAME_SIZE / 5];
            int readCount;
            // 这里我们只测试单声道的，也先不考虑PCM每样点编码比特数,单声道是1，
            int channels = wav.getChannels();
            // 8khz 8000*0.02=160，16 16000*0.02 =320,44100*0.02
            int inputSize = channels * PCM_FRAME_SIZE;
            readCount = wav.read(speech, inputSize);
            // 跳过有问题的帧
            if (readCount != PCM_FRAME_SIZE) {
                Log.e(TAG, "READ FILE ERROR!");
                break;
            }
            int outLength = AmrEncoder.encodeAmr(
                    reqMode, speech, outbuf);
            mOutStream.write(outbuf, 0, outLength);
            bytecount += outLength;
        }
        amrFile.setFileSize(bytecount + 6);
        wav.closeWaveFile();
        mOutStream.close();
        AmrEncoder.exitEncAmr(mNativeAmrEncoder);
        return finfor;
    }

}
