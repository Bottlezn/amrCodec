package cn.wzh.amrcodec.sample;

import android.os.Handler;
import android.os.Looper;

/**
 * author: wangzh
 * create: 2018/11/30 16:55
 * description: TODO
 * version: 1.0
 */
public final class MainHandler extends Handler {

    private static volatile MainHandler sInstance;

    public static MainHandler getInstance() {
        if (null == sInstance) {
            synchronized (MainHandler.class) {
                if (null == sInstance) {
                    sInstance = new MainHandler();
                }
            }
        }
        return sInstance;
    }

    public void postRunnable(Runnable runnable) {
        sInstance.post(runnable);
    }


    private MainHandler() {
        super(Looper.getMainLooper());
    }
}

