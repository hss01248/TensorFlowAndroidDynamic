package com.mindorks.tensorflowexample;

import android.Manifest;
import android.content.Context;
import android.support.annotation.NonNull;

import com.yanzhenjie.permission.AndPermission;

import java.util.List;

/**
 * Created by huangshuisheng on 2017/11/10.
 */

public class MyPermission {
    private static Context context;
    public static void init(Context context){
        MyPermission.context = context;
    }


    /**
     *  compile 'com.mylhyl:acp:1.0.0'
     * @param listener
     * @param permission
     */
    public static void askPermission(final PermissionListener listener,String... permission){


        int ruquestCode = 1024;
        Context context1 = null;
        if(context1 ==null){
            context1 = context;
        }
        final Context finalContext = context1;
        AndPermission.with(context1)
            .requestCode(ruquestCode)
            .permission(permission).callback(new com.yanzhenjie.permission.PermissionListener() {
            @Override
            public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                if(AndPermission.hasPermission(finalContext,grantPermissions)){
                    listener.onGranted(grantPermissions);
                }else {
                    listener.onDenied(grantPermissions);
                }
            }

            @Override
            public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                if(AndPermission.hasPermission(finalContext,deniedPermissions)){
                    listener.onGranted(deniedPermissions);
                }else {
                    listener.onDenied(deniedPermissions);
                }

            }
        }).start();
           // .rationale(...)



        /*if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+
            Acp.getInstance(context).request(new AcpOptions.Builder()
                    .setPermissions(permission)
//                .setDeniedMessage()
//                .setDeniedCloseBtn()
//                .setDeniedSettingBtn()
//                .setRationalMessage()
//                .setRationalBtn()
                    .request(),
                new AcpListener() {
                    @Override
                    public void onGranted() {
                        listener.onGranted();
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        listener.onDenied(permissions);
                        // MyToast.showFailToast("权限已经被拒绝");
                    }
                });
        } else {
            // Pre-Marshmallow
            listener.onGranted();
        }*/
    }

    /**
     * group:android.permission-group.CALENDAR
     permission:android.permission.READ_CALENDAR
     permission:android.permission.WRITE_CALENDAR
     */
    public static void askCalendar(PermissionListener listener){
        askPermission(listener, Manifest.permission.READ_CALENDAR);
    }

    /**
     * group:android.permission-group.CAMERA
     permission:android.permission.CAMERA
     */
    public static void askCamera(PermissionListener listener){
        askPermission(listener, Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }


    /**
     * group:android.permission-group.STORAGE
     permission:android.permission.READ_EXTERNAL_STORAGE
     permission:android.permission.WRITE_EXTERNAL_STORAGE
     */
    public static void askExternalStorage(PermissionListener listener){
        askPermission(listener, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * group:android.permission-group.PHONE
     *
     permission:android.permission.READ_CALL_LOG
     permission:android.permission.READ_PHONE_STATE
     permission:android.permission.CALL_PHONE
     permission:android.permission.WRITE_CALL_LOG
     permission:android.permission.USE_SIP
     permission:android.permission.PROCESS_OUTGOING_CALLS
     permission:com.android.voicemail.permission.ADD_VOICEMAIL
     */
    public static void askPhone(PermissionListener listener){
        askPermission(listener, Manifest.permission.READ_PHONE_STATE);
    }
    public static void askCallPhone(PermissionListener listener){
        askPermission(listener, Manifest.permission.CALL_PHONE);
    }

    /**
     * group:android.permission-group.SMS
     *
     permission:android.permission.READ_SMS
     permission:android.permission.RECEIVE_WAP_PUSH
     permission:android.permission.RECEIVE_MMS
     permission:android.permission.RECEIVE_SMS
     permission:android.permission.SEND_SMS
     permission:android.permission.READ_CELL_BROADCASTS
     */
    public static void askSms(PermissionListener listener){
        askPermission(listener, Manifest.permission.SEND_SMS);
    }




    /**
     * group:android.permission-group.LOCATION
     permission:android.permission.ACCESS_FINE_LOCATION
     permission:android.permission.ACCESS_COARSE_LOCATION
     */
    public static void askLocationInfo(PermissionListener listener){
        askPermission(listener, Manifest.permission.ACCESS_COARSE_LOCATION);
    }


    /**
     * group:android.permission-group.MICROPHONE
     permission:android.permission.RECORD_AUDIO
     */
    public static void askRecord(PermissionListener listener){
        askPermission(listener, Manifest.permission.RECORD_AUDIO);
    }


    /**
     * group:android.permission-group.SENSORS
     permission:android.permission.BODY_SENSORS
     */
    public static void askSensors(PermissionListener listener){
        askPermission(listener, Manifest.permission.BODY_SENSORS);
    }

    /**
     * group:android.permission-group.CONTACTS
     permission:android.permission.WRITE_CONTACTS
     permission:android.permission.GET_ACCOUNTS
     permission:android.permission.READ_CONTACTS
     */
    public static void askContacts(PermissionListener listener){
        askPermission(listener, Manifest.permission.READ_CONTACTS);
    }

    public interface  PermissionListener{
        void onGranted(List<String> permissions);
        void onDenied(List<String> permissions);

    }
}
