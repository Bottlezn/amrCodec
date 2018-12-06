package cn.wzh.amrcodec.encoder;

/**
 * author: wangzh
 * create: 2018/11/30 10:58
 * description: amr文件的解码，目前只支持8kHz采样频率的文件解析
 * version: 1.0
 */
public class AmrEncoder {

    static {
        System.loadLibrary("amr-codec");
    }


    public static native int initEncAmr(int ini);

    public static native void exitEncAmr(int encode);

    public static native int encodeAmr(AmrMode mode, short[] in, byte[] out);

}
