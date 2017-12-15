/*
 *    Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.mindorks.tensorflowexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.hss01248.notifyutil.NotifyUtil;
import com.hss01248.tensorflowdynamic.TfFileDownloader;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import es.dmoral.toasty.MyToast;

public class MainActivity extends AppCompatActivity {

    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";

    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/imagenet_comp_graph_label_strings.txt";
    public static final String PB_URL = "https://github.com/hss01248/TensorFlowAndroidDynamic/blob/master/old/tensorflow_inception_graph.pb?raw=true";

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private Button btnDetectObject, btnToggleCamera;
    private ImageView imageViewResult;
    private CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = (CameraView) findViewById(R.id.cameraView);
        imageViewResult = (ImageView) findViewById(R.id.imageViewResult);
        textViewResult = (TextView) findViewById(R.id.textViewResult);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());

        btnToggleCamera = (Button) findViewById(R.id.btnToggleCamera);
        btnDetectObject = (Button) findViewById(R.id.btnDetectObject);

        cameraView.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                super.onPictureTaken(picture);

                Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);

                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

                imageViewResult.setImageBitmap(bitmap);

                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

                textViewResult.setText(results.toString());
            }
        });

        btnToggleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.toggleFacing();
            }
        });

        btnDetectObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
            }
        });


        downloadFile();


        //initTensorFlowAndLoadModel();
    }

    private void downloadFile() {

        TfFileDownloader.downloadSo("", new TfFileDownloader.DownloadCallback() {
            @Override
            public void onPending(String url) {

            }

            @Override
            public void onSuccess(String url, String filePath) {
                try {
                    NotifyUtil.cancel(8988);
                    System.load(filePath);
                    downloadPb();
                    MyToast.success("download success");
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onProgress(String fileName, int soFarBytes, int totalBytes) {
                NotifyUtil.buildProgress(8988,R.mipmap.ic_launcher,fileName,soFarBytes,totalBytes,"").show();
            }

            @Override
            public void onFail(String url, Throwable e) {
                e.printStackTrace();
                NotifyUtil.cancel(8988);

            }
        });
        /*try {
            Log.e("jjj", "[copySo] supported api:" + Build.CPU_ABI + "--- " + Build.CPU_ABI2);
            String fileInSd = new File(Environment.getExternalStorageDirectory(),"libtensorflow_inference.so").getAbsolutePath();
            String newFile = new File(TfFileDownloader.context.getDir("jniLibs", Context.MODE_PRIVATE),"libtensorflow_inference.so").getAbsolutePath();

            boolean success = TfFileDownloader.copyFile(fileInSd,newFile);
            if(success){
                try {
                    System.load(newFile);
                    //downloadPb();
                    MyToast.success("download success");
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }else {
                MyToast.success("copy fail");
            }
        }catch (Exception e){
            e.printStackTrace();
        }*/



       /* TfFileDownloader.download(true, new TfFileDownloader.DownloadCallback() {
            @Override
            public void onSuccess(String url, String filePath) {
                //使用load方法加载内部储存的SO库
                //ClassLoader referenced unknown path: /data/app/com.mindorks.tensorflowexample-2/lib/arm64
                try {
                    Log.e("dd",filePath);
                    System.load(filePath);
                   // downloadPb();
                    MyToast.success("download success");
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onProgress(String fileName, int soFarBytes, int totalBytes) {

            }

            @Override
            public void onFail(String url, Throwable e) {

            }
        });*/


    }

    private void downloadPb() {
        TfFileDownloader.downPb(PB_URL,
            "old/tensorflow_inception_graph.pb",
            "",
            new TfFileDownloader.DownloadCallback() {
                @Override
                public void onPending(String url) {

                }

                @Override
            public void onSuccess(String url, String filePath) {
                //使用load方法加载内部储存的SO库
                try {
                    NotifyUtil.cancel(8989);
                    initTensorFlowAndLoadModel(filePath);
                    MyToast.success("download success");
                }catch (Exception e){
                    e.printStackTrace();
                }


            }

            @Override
            public void onProgress(String fileName, int soFarBytes, int totalBytes) {
                NotifyUtil.buildProgress(8989,R.mipmap.ic_launcher,fileName,soFarBytes,totalBytes,"").show();
            }

            @Override
            public void onFail(String url, Throwable e) {
                NotifyUtil.cancel(8989);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

    private void initTensorFlowAndLoadModel(final String filePath) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                        filePath,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                    makeButtonVisible();

                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility(View.VISIBLE);
                MyToast.successBig("success initializing TensorFlow");
            }
        });
    }
}
