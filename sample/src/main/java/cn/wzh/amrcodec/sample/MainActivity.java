package cn.wzh.amrcodec.sample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.wzh.amrcodec.bean.FileInfo;
import cn.wzh.amrcodec.encoder.AmrMode;
import cn.wzh.amrcodec.exception.InvalidWaveException;
import cn.wzh.amrcodec.sample.consts.RecordConst;
import cn.wzh.amrcodec.utils.Pcm2Wav;
import cn.wzh.amrcodec.utils.Wav2Amr;

/**
 * author: wangzh
 * create: 2018/11/30 14:27
 * description: 录制一段wav录音，之后转换成amr录音
 * version: 1.0
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tvRecordStatus)
    TextView tvRecordStatus;
    @BindView(R.id.tvWav)
    TextView tvWav;
    @BindView(R.id.tvAmr)
    TextView tvAmr;
    @BindView(R.id.tvPcm)
    TextView tvPcm;
    private String mRootPath;

    private String mPcmPath;

    private String mWavPath;

    private String mAmrPath;

    private RecordCallback mRecordCallback = new RecordCallback() {
        @Override
        public void createFile(boolean result) {
            showToast("创建文件的结果是 " + result);
            tvRecordStatus.setText(String.valueOf("创建文件的结果是 " + result));
        }

        @Override
        public void finished(File outputFile) {
            mPcmPath = outputFile.getAbsolutePath();
            showToast("录制结束，outputFile = " + mPcmPath);
            tvRecordStatus.setText(String.valueOf("录制结束，outputFile = " + mPcmPath));
            setFileStatus(tvPcm, outputFile.getAbsolutePath());
            mProgressDialog.dismiss();
        }

        @Override
        public void error(Throwable t) {
            mProgressDialog.dismiss();
            t.printStackTrace();
            showToast("录制失败，" + t.getMessage());
        }
    };

    private void setFileStatus(final TextView tv, final String filePath) {
        MainHandler.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                File file = new File(filePath);
                String builder = "filePath -> " + filePath + "\n" +
                        "fileSize -> " + file.length();
                tv.setText(builder);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        return super.dispatchTouchEvent(ev);
    }

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCanceledOnTouchOutside(false);
        ButterKnife.bind(this);
        File root = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "testWav2Amr");
        if (!root.exists()) {
            root.mkdirs();
        }
        mRootPath = root.getAbsolutePath();
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                1);
    }

    private void showToast(final String text) {
        MainHandler.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick({R.id.btnStartRecord,
            R.id.btnPauseRecord,
            R.id.btnResumeRecord,
            R.id.btnStopRecord,
            R.id.btnWav2Amr,
            R.id.btnPlayWav,
            R.id.btnPcm2Wav,
            R.id.btnPlayPcm,
            R.id.btnPlayAmr})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnStartRecord:
                AudioRecordManager.getInstance().startRecord(mRootPath + File.separator + getDate(),
                        mRecordCallback);
                break;
            case R.id.btnPauseRecord:
                AudioRecordManager.getInstance().onPause();
                break;
            case R.id.btnResumeRecord:
                AudioRecordManager.getInstance().onResume();
                break;
            case R.id.btnStopRecord:
                mProgressDialog.show();
                AudioRecordManager.getInstance().stopRecord();
                break;
            case R.id.btnPcm2Wav:
                if (TextUtils.isEmpty(mPcmPath)) {
                    showToast("请录制Pcm录音文件");
                    return;
                }
                mProgressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mWavPath = getWavFilePath(mPcmPath);
                        Pcm2Wav.convertPcm2Wav(mPcmPath,
                                mWavPath,
                                RecordConst.SAMPLE_RATE_IN_HZ,
                                1,
                                16);
                        MainHandler.getInstance().postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                setFileStatus(tvWav, mWavPath);
                                mProgressDialog.dismiss();
                            }
                        });
                    }
                }).start();
                break;
            case R.id.btnWav2Amr:
                if (TextUtils.isEmpty(mWavPath)) {
                    showToast("请先录制wav音频");
                    return;
                }
                mProgressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mAmrPath = getAmrFilePath(mWavPath);
                        Wav2Amr wav2Amr = new Wav2Amr();
                        try {
                            FileInfo[] fileInfos = wav2Amr.convertwav(new File(mWavPath),
                                    new File(mAmrPath),
                                    AmrMode.MR475,
                                    RecordConst.SAMPLE_RATE_IN_HZ);
                            for (FileInfo info : fileInfos) {
                                Log.i("WTF", info.toString());
                            }
                            showToast("转换成功，路径是" + mAmrPath);
                            MainHandler.getInstance().postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    setFileStatus(tvAmr, mAmrPath);
                                }
                            });
                        } catch (final IOException e) {
                            e.printStackTrace();
                            MainHandler.getInstance().postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    showToast("转换失败 -> " + e.getMessage());
                                }
                            });
                        } catch (InvalidWaveException e) {
                            e.printStackTrace();
                        }

                        MainHandler.getInstance().postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                            }
                        });
                    }
                }).start();
                break;
            case R.id.btnPlayPcm:
                if (!TextUtils.isEmpty(mPcmPath)) {
                    play(mPcmPath);
                }
                break;
            case R.id.btnPlayWav:
                if (TextUtils.isEmpty(mWavPath)) {
                    showToast("请转换wav音频");
                    return;
                }
                playAudio(mWavPath, "wav");
                break;
            case R.id.btnPlayAmr:
                if (TextUtils.isEmpty(mAmrPath)) {
                    showToast("请转换amr音频");
                    return;
                }
                playAudio(mAmrPath, "amr");
                break;
        }
    }

    /**
     * 播放指定名称的歌曲
     *
     * @param file 指定默认播放的音乐
     */
    public void playAudio(String file, String suffix) {
        try {
            Uri uri = null;
            if (Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", new File(file));
            } else {
                uri = Uri.fromFile(new File(file));
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, "audio/" + suffix);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDate() {
        return new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.CHINA).format(new Date());
    }

    private String getWavFilePath(String pcmFile) {
        return pcmFile.substring(0, pcmFile.length() - 4) + ".wav";
    }

    private String getAmrFilePath(String wavFile) {
        return wavFile.substring(0, wavFile.length() - 4) + ".amr";
    }

    //播放音频（PCM）
    public void play(final String pcmFile) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataInputStream dis = null;
                try {
                    //从音频文件中读取声音
                    dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(pcmFile))));

                    //最小缓存区
                    int bufferSizeInBytes = AudioTrack.getMinBufferSize(RecordConst.SAMPLE_RATE_IN_HZ,
                            AudioFormat.CHANNEL_OUT_MONO,
                            RecordConst.AUDIO_FORMAT);
                    //创建AudioTrack对象   依次传入 :流类型、采样率（与采集的要一致）、音频通道（采集是IN 播放时OUT）、量化位数、最小缓冲区、模式
                    AudioTrack player = new AudioTrack(AudioManager.STREAM_MUSIC,
                            RecordConst.SAMPLE_RATE_IN_HZ,
                            AudioFormat.CHANNEL_OUT_MONO,
                            RecordConst.AUDIO_FORMAT,
                            bufferSizeInBytes,
                            AudioTrack.MODE_STREAM);

                    byte[] data = new byte[bufferSizeInBytes];
                    player.play();//开始播放
                    while (true) {
                        int i = 0;
                        try {
                            while (dis.available() > 0 && i < data.length) {
                                data[i] = dis.readByte();//录音时write Byte 那么读取时就该为readByte要相互对应
                                i++;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        player.write(data, 0, data.length);
                        //表示读取完了
                        if (i != bufferSizeInBytes) {
                            player.stop();//停止播放
                            player.release();//释放资源
                            break;
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    showToast("播放音频失败 , " + e.getMessage());
                } finally {
                    if (dis != null) {
                        try {
                            dis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

}
