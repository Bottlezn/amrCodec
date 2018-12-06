package cn.wzh.amrcodec.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import cn.wzh.amrcodec.bean.FileInfo;
import cn.wzh.amrcodec.decoder.AmrDecoder;
import cn.wzh.amrcodec.io.WaveWriter;

/**
 * author: wangzh
 * create: 2018/11/30 11:02
 * description: 将amr文件转换成wav文件,源文件的格式必须是amr文件，否则无效
 * version: 1.0
 */
public class Amr2wav {

    private static final String TAG = Amr2wav.class.getSimpleName();

    /* From WmfDecBytesPerFrame in dec_input_format_tab.cpp */
    private  int sizes[] = {12, 13, 15, 17, 19, 20, 26, 31, 5, 6, 5, 5, 0, 0, 0, 0};


    public FileInfo[] convertamr(File input, File output) throws IOException {

        byte[] header = new byte[6];
        int fileSize = (int) input.length();
        Log.w(TAG, "fileSize = " + fileSize);
        FileInfo[] finfor = new FileInfo[2];
        /* amr文件输入信息，这里我们测试的是单声道的文件，文件头开始是“#AMR!/n” */

        FileInfo amrFile = new FileInfo();
        FileInfo wavFile = new FileInfo();

        finfor[0] = amrFile;
        finfor[1] = wavFile;
        amrFile.setFileType("amr");
        amrFile.setFileSize(fileSize);
        amrFile.setSampleRate(8000);
        amrFile.setBitsPerSample(16);
        amrFile.setChannels(1);

        FileInputStream in = null;
        int count = 0;
        try {
            in = new FileInputStream(input);
            count = in.read(header, 0, 6);
        } catch (Exception e) {

            e.printStackTrace();
            System.out.println("读入文件错误！");
            return finfor;
        }
        System.out.println("开始创建文件！");
        if (count != 6 || header[0] != '#' || header[1] != '!'
                || header[2] != 'A' || header[3] != 'M' || header[4] != 'R'
                || header[5] != '\n') {
            System.out.println("BAD HEADER"); // 检查文件头是否是由#！AMR/n开始的
        }
        // the pointer to the native
        int mNativeAmrDecoder = AmrDecoder.initDecamr();
        System.out.println("开始创建文件1！");

        // 创建WaveWriter对象
        wavFile.setFileType("wav");
        wavFile.setSampleRate(8000);
        wavFile.setBitsPerSample(16);
        wavFile.setChannels(1);

        WaveWriter wav = new WaveWriter(output, wavFile.getSampleRate(),
                wavFile.getChannels(), wavFile.getBitsPerSample());

        boolean flag = wav.createWaveFile();
        if (!flag) {
            System.out.println("Failed to createWaveFile.");
            in.close();
            return finfor;
        }
        int counter = 0;
        while (true) {
            byte[] buffer = new byte[500];

            // 读入模式字节
            int n = in.read(buffer, 0, 1);
            if (n != 1)
                break;
            // 按照模式字节显示的数据包的大小来读数据
            int size = sizes[(buffer[0] >> 3) & 0x0f];
            if (size <= 0)
                break;
            n = in.read(buffer, 1, size);
            if (n != size)
                break;

            short[] outbuffer = new short[160];
            counter++;
            System.out.println(counter);
            // System.out.println("开始写入wav文件！");
            AmrDecoder.decodeAmr(n, buffer, outbuffer, 0);
            byte littleendian[] = new byte[320];
            int j = 0;
            for (int i = 0; i < 160; i++) {
                littleendian[j] = (byte) (outbuffer[i] & 0xff);
                littleendian[j + 1] = (byte) (outbuffer[i] >> 8 & 0xff);
                j = j + 2;
            }
            wav.write(littleendian, 0, 320);
        }
        //wav文件大小
        wavFile.setFileSize(wav.getBytesWritten() + 44);
        wav.closeWaveFile();
        in.close();
        Log.i(TAG, "wav文件写完啦");
        AmrDecoder.exitDecAmr(mNativeAmrDecoder);
        return finfor;

    }
}
