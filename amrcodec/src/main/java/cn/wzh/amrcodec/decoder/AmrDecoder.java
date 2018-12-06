package cn.wzh.amrcodec.decoder;

/**
 * author: wangzh
 * create: 2018/11/30 11:00
 * description: Amr编码工具
 * version: 1.0
 */
public class AmrDecoder {
    static{
        System.loadLibrary("amr-codec");
    }

    public static native long initDecamr();

    public static native void exitDecAmr(int decode);

    public static native void decodeAmr(int gae, byte[] in, short[] outbuffer, int unused);

}
