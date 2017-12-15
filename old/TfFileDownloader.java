package com.mindorks.tensorflowexample;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import static android.content.ContentValues.TAG;

/**
 * Created by huangshuisheng on 2017/12/14.
 */

public class TfFileDownloader {

    public static final String SO_URL = "http://10.0.16.48/hss/libtensorflow_inference.so";
    public static final String PB_URL = "http://10.0.16.48/hss/tensorflow_inception_graph.pb";
    public static Context context;

    public static void init(Context context){
            TfFileDownloader.context = context;
            FileDownloader.setup(context);
    }

    public interface DownloadCallback{
            void onSuccess(String url,String filePath);
            void onProgress(String fileName,int soFarBytes, int totalBytes);
            void onFail(String url,Throwable e);
    }

    public static void download(boolean isSo, final DownloadCallback callback){
            String fileName = "libtensorflow_inference.so";
            String url = SO_URL;
            if(isSo){
                    fileName = "libtensorflow_inference.so";
                    url = SO_URL;
            }else {
                    fileName = "tensorflow_inception_graph.pb";
                    url = PB_URL;
            }
            File dir = null;
            if(isSo){
                dir = context.getFilesDir();//jniLibs,libs,"jniLibs",Context.MODE_PRIVATE
            }else {
                dir = context.getFilesDir();
            }
            final String path = new File(dir,fileName).getAbsolutePath();
        Log.e("ddpath",path);

            final String finalUrl = url;
        final String finalFileName = fileName;
        FileDownloader.getImpl().create(url)
            .setPath(path)
            .setListener(new FileDownloadListener() {
                @Override
                protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                }

                @Override
                protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                }

                @Override
                protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        String format = String.format("url:%s,totalBytes:%d,soFarBytes:%d", finalUrl,totalBytes,soFarBytes);
                        Log.e("progress",format);
                        callback.onProgress(finalFileName,soFarBytes,totalBytes);
                }

                @Override
                protected void blockComplete(BaseDownloadTask task) {
                }

                @Override
                protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                }

                @Override
                protected void completed(BaseDownloadTask task) {
                        callback.onSuccess(finalUrl,path);
                        Log.e("completed",finalUrl);

                }

                @Override
                protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                }

                @Override
                protected void error(BaseDownloadTask task, Throwable e) {
                        callback.onFail(finalUrl,e);
                        Log.e("error",finalUrl);
                        if(e!=null){
                            e.printStackTrace();
                        }
                }

                @Override
                protected void warn(BaseDownloadTask task) {
                }
            }).start();
    }

    public static String  getSupportedAbi(){
        String abi1 = "";

        if (Build.VERSION.SDK_INT >= 21) {
            String[] abis = Build.SUPPORTED_ABIS;
            if (abis != null) {
                for (String abi : abis) {
                    Log.d(TAG, "[copySo] supported api:" + abi);
                    abi1 = abi;
                }
            }
        }else {
            Log.d(TAG, "[copySo] supported api:" + Build.CPU_ABI + "--- " + Build.CPU_ABI2);
            if(!TextUtils.isEmpty(Build.CPU_ABI)){
                abi1 = Build.CPU_ABI;
            }else if(!TextUtils.isEmpty(Build.CPU_ABI2)){
                abi1 = Build.CPU_ABI2;
            }
        }
        return abi1;

    }


    /**
     * 将一个SO库复制到指定路径，会先检查改SO库是否与当前CPU兼容
     *
     * @param sourceDir     SO库所在目录
     * @param so            SO库名字
     * @param destDir       目标根目录
     * @param nativeLibName 目标SO库目录名
     * @return
     */
    public static boolean copySoLib(File sourceDir, String so, String destDir, String nativeLibName) throws IOException {

        boolean isSuccess = false;
        try {
            Log.d(TAG, "[copySo] 开始处理so文件");

            if (Build.VERSION.SDK_INT >= 21) {
                String[] abis = Build.SUPPORTED_ABIS;
                if (abis != null) {
                    for (String abi : abis) {
                        Log.d(TAG, "[copySo] try supported abi:" + abi);
                        String name = "lib" + File.separator + abi + File.separator + so;
                        File sourceFile = new File(sourceDir, name);
                        if (sourceFile.exists()) {
                            Log.i(TAG, "[copySo] copy so: " + sourceFile.getAbsolutePath());
                            isSuccess = copyFile(sourceFile.getAbsolutePath(), destDir + File.separator + nativeLibName + File.separator + so);
                            //api21 64位系统的目录可能有些不同
                            //copyFile(sourceFile.getAbsolutePath(), destDir + File.separator +  name);
                            break;
                        }
                    }
                } else {
                    Log.e(TAG, "[copySo] get abis == null");
                }
            } else {
                Log.d(TAG, "[copySo] supported api:" + Build.CPU_ABI + "--- " + Build.CPU_ABI2);

                String name = "lib" + File.separator + Build.CPU_ABI + File.separator + so;
                File sourceFile = new File(sourceDir, name);

                if (!sourceFile.exists() && Build.CPU_ABI2 != null) {
                    name = "lib" + File.separator + Build.CPU_ABI2 + File.separator + so;
                    sourceFile = new File(sourceDir, name);

                    if (!sourceFile.exists()) {
                        name = "lib" + File.separator + "armeabi" + File.separator + so;
                        sourceFile = new File(sourceDir, name);
                    }
                }
                if (sourceFile.exists()) {
                    Log.i(TAG, "[copySo] copy so: " + sourceFile.getAbsolutePath());
                    isSuccess = copyFile(sourceFile.getAbsolutePath(), destDir + File.separator + nativeLibName + File.separator + so);
                }
            }

            if (!isSuccess) {
                Log.e(TAG, "[copySo] 安装 " + so + " 失败 : NO_MATCHING_ABIS");
                throw new IOException("install " + so + " fail : NO_MATCHING_ABIS");
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        return true;
    }

    public static boolean copyFile(String originalPath, String destPath) {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(originalPath);
            output = new FileOutputStream(destPath);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        } finally {
            try {
                input.close();
                output.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }






}
