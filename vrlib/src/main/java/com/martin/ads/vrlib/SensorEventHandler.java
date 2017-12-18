package com.martin.ads.vrlib;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.martin.ads.vrlib.constant.Constants;
import com.martin.ads.vrlib.constant.PanoStatus;
import com.martin.ads.vrlib.database.HistoryFrame;
import com.martin.ads.vrlib.database.MyDataBase;
import com.martin.ads.vrlib.ui.PanoPlayerActivity;
import com.martin.ads.vrlib.utils.SensorUtils;
import com.martin.ads.vrlib.utils.StatusHelper;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static com.martin.ads.vrlib.database.HistoryFrame.mList;
import static com.martin.ads.vrlib.ui.PanoPlayerActivity.mHandler;

/**
 * Project: Pano360
 * Package: com.martin.ads.pano360
 * Created by Ads on 2016/5/2.
 */
public class SensorEventHandler implements SensorEventListener {

    public static String TAG = "SensorEventHandler";
    private Message msg = new Message();

    private float[] rotationMatrix = new float[16];
    private static float[] Temp = new float[16];
    private Timer timer =new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            Temp = rotationMatrix;
        }
    };
    //private float[] orientationData=new float[3];
    private MyDataBase myDataBase = new MyDataBase(PanoPlayerActivity.getmPanoPlayerActivity().getApplicationContext());
    private HistoryFrame mHistoryFrame = new HistoryFrame();
    private float[] temp = new float[16];
    private float[] zero = {
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0
    };
    private boolean a = true;

    private StatusHelper statusHelper;
    private SensorHandlerCallback sensorHandlerCallback;

    private boolean sensorRegistered;
    private SensorManager sensorManager;

    private int mDeviceRotation;

    public void init(){
        sensorRegistered=false;
        sensorManager = (SensorManager) statusHelper.getContext()
                .getSystemService(Context.SENSOR_SERVICE);
        Sensor sensorRot = sensorManager.getDefaultSensor(Constants.SENSOR_ROT);
        if (sensorRot==null) return;
        sensorManager.registerListener(this, sensorRot, SensorManager.SENSOR_DELAY_GAME);
        sensorRegistered=true;

//        initMyDataBase();
        timer.schedule(timerTask,0,500);
    }

    public void releaseResources(){
        if (!sensorRegistered || sensorManager==null) return;
        sensorManager.unregisterListener(this);
        sensorRegistered = false;
    }

    public void initMyDataBase(){
        myDataBase.delet();
        for (int i = 0; i < 2000; i++) {
            mHistoryFrame.historyFrame = zero;
            mList.add(i, mHistoryFrame);
        }
    }

//    public boolean PauseFun(){
//        float[] v1 = new float[3];
//        float[] v2 = new float[3];
//        SensorManager.getOrientation(mList.get(0).historyFrame, v1);
//        SensorManager.getOrientation(mList.get(1999).historyFrame, v2);
//        Log.i("SOS",String.valueOf(v1[0]) + "   " + String.valueOf(v2[0]));
//        if((Math.abs(v1[0] - v2[0]) > 3) || (Math.abs(v1[1] - v2[1]) > 3) || (Math.abs(v1[2] - v2[2]) > 3)){
//            return true;
//        }else {
//            return false;
//        }
//    }

    public boolean PauseFun(float[] temp, float[] value){
        float[] v1 = new float[3];
        float[] v2 = new float[3];
        SensorManager.getOrientation(temp, v1);
        SensorManager.getOrientation(value, v2);
        Log.i("SOS",String.valueOf(Math.abs(v1[0] - v2[0]))
                +"   "+ String.valueOf(Math.abs(v1[1] - v2[1]))
                +"   "+ String.valueOf(Math.abs(v1[2] - v2[2])));
        if((Math.abs(v1[0] - v2[0]) > 3) || (Math.abs(v1[1] - v2[1]) > 3) || (Math.abs(v1[2] - v2[2]) > 6)){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy != 0){
            int type = event.sensor.getType();
            switch (type){
                case Constants.SENSOR_ROT:
                    //FIXME
                    mDeviceRotation= ((Activity)statusHelper.getContext()).getWindowManager().getDefaultDisplay().getRotation();
                    SensorUtils.sensorRotationVectorToMatrix(event,mDeviceRotation,rotationMatrix);


//                    mHistoryFrame.historyFrame = rotationMatrix;
//                    mList.add(mHistoryFrame);
//                    Log.i("Length",String.valueOf(mHistoryFrame.historyFrame[0]));
//                    if((!Arrays.equals(mList.removeFirst().historyFrame, zero))){
//                        Log.i("Arr","full");
                        //如果链表第一个是空，则不启用暂停功能直到链表被装满
                        if(PauseFun(Temp,rotationMatrix) && a && !statusHelper.getPanoPauseChangeMode()){
//                            Log.i("Arr",String.valueOf(PauseFun()));
                            myDataBase.delet();
                            myDataBase.insert(rotationMatrix);
                            temp = myDataBase.query(1);
                            a = false;
                            sensorHandlerCallback.updateSensorMatrix(temp);
                            mHandler.sendEmptyMessage(0);

                            break;
                        }else if(a){
                            sensorHandlerCallback.updateSensorMatrix(rotationMatrix);
                            break;
                        }else if(statusHelper.getPanoPauseChangeMode()){
                            sensorHandlerCallback.updateSensorMatrix(rotationMatrix);
                            statusHelper.setPanoPauseChangeMode(false);
                            mHandler.sendEmptyMessage(1);
                            a = true;
                            break;
                        }else {
                            sensorHandlerCallback.updateSensorMatrix(temp);
                            break;
                        }
//                    }else {
//                        sensorHandlerCallback.updateSensorMatrix(rotationMatrix);
//                        break;
//                    }

//
//                    sensorHandlerCallback.updateSensorMatrix(rotationMatrix);
//                break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setSensorHandlerCallback(SensorHandlerCallback sensorHandlerCallback){
        this.sensorHandlerCallback=sensorHandlerCallback;
    }

    public void setStatusHelper(StatusHelper statusHelper){
        this.statusHelper=statusHelper;
    }

    public interface SensorHandlerCallback{
        void updateSensorMatrix(float[] sensorMatrix);
    }
}
