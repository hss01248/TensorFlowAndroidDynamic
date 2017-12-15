package com.hss01248.tensorflowdynamic;

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
import java.security.MessageDigest;

/**
 * Created by huangshuisheng on 2017/12/14.
 */

public class TfFileDownloader {



    public static Context context;

    public static String SO_URL_ARM64 ;
    public static String SO_URL_V7 ;
    public static String SO_URL_X86 ;
    public static String SO_URL_X86_64 ;

    public static final String TAG = "TfFileDownloader";

    private static final String SO_NAME = "libtensorflow_inference.so";
    private static boolean showLog;


    public static void init(Context context,boolean showLog) {
        TfFileDownloader.context = context;
        FileDownloader.setup(context);
        TfFileDownloader.showLog = showLog;
    }

    public static void setSoUrl(String arm64, String v7, String x86, String x8664) {
        SO_URL_ARM64 = arm64;
        SO_URL_V7 = v7;
        SO_URL_X86 = x86;
        SO_URL_X86_64 = x8664;

    }

    public interface DownloadCallback {
        void onSuccess(String url, String filePath);

        void onProgress(String fileName, int soFarBytes, int totalBytes);

        void onFail(String url, Throwable e);
    }


    /**
     * @param md5      用于校验下载后的文件是否正确,如果不需要校验,可以传入空
     * @param callback
     */
    public static void downloadSo(final String md5, final DownloadCallback callback) {

        String abi = getSupportedAbi();
        if (TextUtils.isEmpty(abi)) {
            callback.onFail("", new Throwable("support abi is empty:"));
            return;
        }
        File dir = context.getDir("jnilibs", Context.MODE_PRIVATE);
        boolean dirExits = true;
        if (!dir.exists()) {
            dirExits = dir.mkdirs();
        }

        if (!dirExits) {
            callback.onFail("", new Throwable("mk jnilibs dir fail:" + dir.getAbsolutePath()));
            return;
        }

        File subDir = new File(dir, abi);
        if (!subDir.exists()) {
            dirExits = subDir.mkdirs();
        }
        if (!dirExits) {
            callback.onFail("", new Throwable("mk dir fail:" + subDir.getAbsolutePath()));
            return;
        }

        final File soFile = new File(subDir, SO_NAME);
        if (soFile.exists()) {
            callback.onSuccess("", soFile.getAbsolutePath());
            return;
        }
        //没有文件,就去下载
        final String url = getSoUrl(abi);
        if (TextUtils.isEmpty(url)) {
            callback.onFail("", new Throwable("no supported abi url,the abi is:"+abi + "or so url not set(have you call setSoUrl() ? )"));
            return;
        }
        final String path = soFile.getAbsolutePath();

        if(showLog)
        Log.e("ddpath", path);

        final String finalUrl = url;
        final String finalFileName = SO_NAME;
        reallyDownload(md5, finalFileName, finalUrl, path, callback);

    }

    private static void reallyDownload(final String md5, final String finalFileName, final String finalUrl,
                                       final String path, final DownloadCallback callback) {
        FileDownloader.getImpl().create(finalUrl)
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
                    String format = String.format("url:%s,totalBytes:%d,soFarBytes:%d", finalUrl, totalBytes, soFarBytes);
                    Log.e("progress", format);
                    callback.onProgress(finalFileName, soFarBytes, totalBytes);
                }

                @Override
                protected void blockComplete(BaseDownloadTask task) {
                }

                @Override
                protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                }

                @Override
                protected void completed(BaseDownloadTask task) {

                    if (!new File(path).exists()) {
                        callback.onFail(finalUrl, new Throwable("file does not exist:" + path));
                        return;
                    }

                    if (TextUtils.isEmpty(md5)) {
                        callback.onSuccess(finalUrl, path);
                        Log.e("completed", finalUrl);
                        return;
                    }
                    String fileMd5 = fileToMD5(path);
                    String desc = "expect md5:" + md5 + "--file real md5:" + fileMd5;
                    Log.e("md5", desc);
                    if (md5.equals(fileMd5)) {
                        callback.onSuccess(finalUrl, path);
                    } else {
                        callback.onFail(finalUrl, new Throwable("file md5 does not match:" + desc));
                    }
                }

                @Override
                protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                }

                @Override
                protected void error(BaseDownloadTask task, Throwable e) {
                    callback.onFail(finalUrl, e);
                    Log.e("error", finalUrl);
                    if (e != null) {
                        e.printStackTrace();
                    }
                }

                @Override
                protected void warn(BaseDownloadTask task) {
                }
            }).start();
    }

    private static String getSoUrl(String abi) {
        if ("armeabi-v7a".equalsIgnoreCase(abi)) {
            return SO_URL_V7;
        }
        if ("arm64-v8a".equalsIgnoreCase(abi)) {
            return SO_URL_ARM64;
        }
        if ("x86".equalsIgnoreCase(abi)) {
            return SO_URL_X86;
        }
        if ("x86_64".equalsIgnoreCase(abi)) {
            return SO_URL_X86_64;
        }
        return "";
    }

    private static String getSupportedAbi() {
        String abi1 = "";

        if (Build.VERSION.SDK_INT >= 21) {
            String[] abis = Build.SUPPORTED_ABIS;
            if (abis != null) {
                String abistr  = "";
                //第一个是原生支持的,后面的是兼容模式.虽然是兼容,但手动加载时很多并不兼容.
                abi1 = abis[0];
                for (String abi : abis) {
                    abistr = abistr + abi+",";
                }
                if(showLog)
                Log.e(TAG, "[copySo] supported api:" + abistr);
            }
        } else {
            if(showLog)
            Log.e(TAG, "[copySo] supported api:" + Build.CPU_ABI + "--- " + Build.CPU_ABI2);
            if (!TextUtils.isEmpty(Build.CPU_ABI)) {
                abi1 = Build.CPU_ABI;
            } else if (!TextUtils.isEmpty(Build.CPU_ABI2)) {
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
    private static boolean copySoLib(File sourceDir, String so, String destDir, String nativeLibName) throws IOException {

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

    private static boolean copyFile(String originalPath, String destPath) {
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                input.close();
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * @param url
     * @param fileName 只是文件名,不要路径名
     * @param callback
     */
    public static void downPb(String url, String fileName, String md5, final DownloadCallback callback) {
        File dir = context.getFilesDir();
        boolean dirExits = true;
        if (!dir.exists()) {
            dirExits = dir.mkdirs();
        }

        if (!dirExits) {
            callback.onFail("", new Throwable("mk FilesDir fail:" + dir.getAbsolutePath()));
            return;
        }

        final File soFile = new File(dir, fileName);
        if (soFile.exists()) {
            callback.onSuccess("", soFile.getAbsolutePath());
            return;
        }

        reallyDownload(md5, fileName, url, soFile.getAbsolutePath(), callback);

    }


    public static String fileToMD5(String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath); // Create an FileInputStream instance according to the filepath
            byte[] buffer = new byte[1024]; // The buffer to read the file
            MessageDigest digest = MessageDigest.getInstance("MD5"); // Get a MD5 instance
            int numRead = 0; // Record how many bytes have been read
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead); // Update the digest
            }
            byte[] md5Bytes = digest.digest(); // Complete the hash computing
            return convertHashToString(md5Bytes); // Call the function to convert to hex digits
        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close(); // Close the InputStream
                } catch (Exception e) {
                }
            }
        }
    }

    private static String convertHashToString(byte[] hashBytes) {
        String returnVal = "";
        for (int i = 0; i < hashBytes.length; i++) {
            returnVal += Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1);
        }
        return returnVal.toLowerCase();
    }
}
