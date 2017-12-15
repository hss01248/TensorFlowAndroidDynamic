package com.mindorks.tensorflowexample;

import android.app.Application;

import com.hss01248.notifyutil.NotifyUtil;
import com.hss01248.tensorflowdynamic.TfFileDownloader;

import es.dmoral.toasty.MyToast;

/**
 * Created by huangshuisheng on 2017/12/14.
 */

public class BaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TfFileDownloader.init(this,true);
        TfFileDownloader.setSoUrl("http://10.0.16.48/hss/jni/arm64-v8a/libtensorflow_inference.so",
            "http://10.0.16.48/hss/jni/armeabi-v7a/libtensorflow_inference.so",
            "http://10.0.16.48/hss/jni/x86/libtensorflow_inference.so",
            "http://10.0.16.48/hss/jni/x86_64/libtensorflow_inference.so");
        NotifyUtil.init(this);
        MyToast.init(this,true,false);
        MyPermission.init(this);
    }
}
