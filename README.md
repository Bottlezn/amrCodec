**A lib for Java ,that was transform wav file into amr file .It was base on opencore-amr.**
## description
使用opencore-amr编译的库，供java使用。  
可以将wav文件编译成amr文件，目前只支持采样率为16kHz的wav文件。有时间再编译vo-amrwbenc库添加进去以支持16kHz采样率的wav文件转换

## sample
该项目中的sample  
- 录制二倍声源大小的录音，从pcm文件保存为wav文件
- 将wav文件转换成amr文件，在提升录音音量的同时，压缩录音体积

## how to use
1. 可以参照sample中AudioRecordManager录制wav语音文件
2. 调用Wav2Amr.convertWav()方法将wav文件，转换成amr文件，可参照MainActivity
3. 该项目也可以将amr文件转换成wav文件，但是可能存在丢失信息过多的情况