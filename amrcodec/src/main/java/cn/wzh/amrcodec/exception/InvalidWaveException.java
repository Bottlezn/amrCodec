package cn.wzh.amrcodec.exception;

/**
 * author: internet
 * create: 2018/11/30 11:13
 * description: TODO
 * version: 1.0
 */
public class InvalidWaveException extends Exception {

    public InvalidWaveException() {
    }

    public InvalidWaveException(String message) {
        super(message);
    }

    public InvalidWaveException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidWaveException(Throwable cause) {
        super(cause);
    }

    public InvalidWaveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
