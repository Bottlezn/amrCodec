package cn.wzh.amrcodec.encoder;

/**
 * author: wangzh
 * create: 2018/12/3 16:47
 * description: amr编码格式的枚举
 * version: 1.0
 */
public enum AmrMode {
    MR475,/* 4.75 kbps */
    MR515,    /* 5.15 kbps */
    MR59,     /* 5.90 kbps */
    MR67,     /* 6.70 kbps */
    MR74,     /* 7.40 kbps */
    MR795,    /* 7.95 kbps */
    MR102,    /* 10.2 kbps */
    MR122,    /* 12.2 kbps */
    MRDTX,    /* DTX       */
    N_MODES   /* Not Used  */
}
