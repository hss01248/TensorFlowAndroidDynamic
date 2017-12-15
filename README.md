# TensorFlowAndroidDynamic
dynamic load tensorflow so and pb file 



fork from [AndroidTensorFlowMachineLearningExample](https://github.com/MindorksOpenSource/AndroidTensorFlowMachineLearningExample)

update the gradle version,

update the tensorflow java api to 1.4.0

 add dynamic load .so and .pb file feature .

the jar and so file are base on google compile version:   compile 'org.tensorflow:tensorflow-android:1.4.0'



blog:[tensorflow集成到Android以及so库和pb文件的动态加载实践](https://juejin.im/post/5a339c546fb9a0452405e398)



# useage

## init in application oncreate:

```
 TfFileDownloader.init(this,true);
 TfFileDownloader.setSoUrl(String arm64, String v7, String x86, String x8664);
```

 ## download:

```
/**
     * @param md5      用于校验下载后的文件是否正确,如果不需要校验,可以传入空
     * @param callback
     */
    public static void downloadSo(final String md5, final DownloadCallback callback)
    
    /**
     * @param url
     * @param fileName 只是文件名,不要路径名
     * @param callback
     */
    public static void downPb(String url, String fileName, String md5, final DownloadCallback callback) 
```

## callback:

```
public interface DownloadCallback {
        void onSuccess(String url, String filePath);

        void onProgress(String fileName, int soFarBytes, int totalBytes);

        void onFail(String url, Throwable e);
    }
```

# Thanks

[FileDownloader](https://github.com/lingochamp/FileDownloader)

[AndroidTensorFlowMachineLearningExample](https://github.com/MindorksOpenSource/AndroidTensorFlowMachineLearningExample)