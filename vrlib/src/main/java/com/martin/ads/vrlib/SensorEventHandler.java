package com.martin.ads.vrlib;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Message;
import android.util.Log;

import com.martin.ads.vrlib.constant.Constants;
import com.martin.ads.vrlib.database.HistoryFrame;
import com.martin.ads.vrlib.database.MyDataBase;
import com.martin.ads.vrlib.ui.PanoPlayerActivity;
import com.martin.ads.vrlib.utils.StatusHelper;

import java.io.FileInputStream;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import static com.martin.ads.vrlib.database.HistoryFrame.mList;

/**
 * Project: Pano360
 * Package: com.martin.ads.pano360
 * Created by Ads on 2016/5/2.
 */
public class SensorEventHandler implements SensorEventListener {

    public static String TAG = "SensorEventHandler";
    private Message msg = new Message();

    private float[] rotationMatrix = new float[16];
    private static double[] Temp = new double[16];
    public FileInputStream fileInputStream;
    public Scanner in;
//    public ArrayList<float[]> vp_data = new ArrayList<>();
//    public int time = 0 ;
    private Timer timer =new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
//            Temp = new double[]{-0.712158154912234, -0.497834546510341, 0.494966187423792, 0.0, 0.528497905120719, -0.844280701036809, -0.0887697140910368, 0.0, 0.46208303006604 , 0.198370517359904, 0.86436590120582, 0.0, 0.0, 0.0, 0.0, 1.0};
//            for(int i= 0; i < Temp.length; i++){
//                rotationMatrix[i] = (float)Temp[i];
//            }
//            rotationMatrix = readCSV("mdata.csv").get(time);
//            time++;
            in.nextLine();
            if (in.hasNextLine()) {
                String[] lines = in.nextLine().split(",");
                for(int i=0;i<16;i++){
                    rotationMatrix[i] = Float.parseFloat(lines[i]);
                }
//                sensorHandlerCallback.updateSensorMatrix(rotationMatrix);

            }

            sensorHandlerCallback.updateSensorMatrix(rotationMatrix);
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

//    private ArrayList<float[]> readCSV(String path) {
//        ArrayList<float[]> viewport_data = new ArrayList<>();
//        FileInputStream fileInputStream;
//        Scanner in;
//        try {
//            fileInputStream = new FileInputStream(Environment.getExternalStorageDirectory().getCanonicalPath() + "/" + path);
//            //一定要声明为GBK编码,因为默认编码为GBK
//            in = new Scanner(fileInputStream);
//            //舍弃第一行
//            while (in.hasNextLine()) {
//                String[] lines = in.nextLine().split(",");
//                float[] view = new float[16];
//                for(int i=0;i<16;i++){
//                    view[i] = Float.parseFloat(lines[i]);
//                }
//                viewport_data.add(view);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Log.v(TAG,"ReadEnd");
//        return viewport_data;
//    }

    public void init(){
        sensorRegistered=false;
        sensorManager = (SensorManager) statusHelper.getContext()
                .getSystemService(Context.SENSOR_SERVICE);
        Sensor sensorRot = sensorManager.getDefaultSensor(Constants.SENSOR_ROT);
        if (sensorRot==null) return;
        sensorManager.registerListener(this, sensorRot, SensorManager.SENSOR_DELAY_GAME);
        sensorRegistered=true;
        try {
            fileInputStream = new FileInputStream(Environment.getExternalStorageDirectory().getCanonicalPath() + "/" + "mdata.csv");
            in = new Scanner(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
//
//        vp_data = readCSV("mdata.csv");
//        time = 0;
//        initMyDataBase();
        timer.schedule(timerTask,0,100);
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
//        if (event.accuracy != 0){
//            int type = event.sensor.getType();
//            switch (type){
//                case Constants.SENSOR_ROT:
//                    //FIXME
//                    mDeviceRotation= ((Activity)statusHelper.getContext()).getWindowManager().getDefaultDisplay().getRotation();
//                    SensorUtils.sensorRotationVectorToMatrix(event,mDeviceRotation,rotationMatrix);
//
//
////                    mHistoryFrame.historyFrame = rotationMatrix;
////                    mList.add(mHistoryFrame);
////                    Log.i("Length",String.valueOf(mHistoryFrame.historyFrame[0]));
////                    if((!Arrays.equals(mList.removeFirst().historyFrame, zero))){
////                        Log.i("Arr","full");
//                        //如果链表第一个是空，则不启用暂停功能直到链表被装满
//                        if(PauseFun(Temp,rotationMatrix) && a && !statusHelper.getPanoPauseChangeMode()){
////                            Log.i("Arr",String.valueOf(PauseFun()));
//                            myDataBase.delet();
//                            myDataBase.insert(rotationMatrix);
//                            temp = myDataBase.query(1);
//                            a = false;
//                            sensorHandlerCallback.updateSensorMatrix(temp);
//                            mHandler.sendEmptyMessage(0);
//
//                            break;
//                        }else if(a){
//                            sensorHandlerCallback.updateSensorMatrix(rotationMatrix);
//                            break;
//                        }else if(statusHelper.getPanoPauseChangeMode()){
//                            sensorHandlerCallback.updateSensorMatrix(rotationMatrix);
//                            statusHelper.setPanoPauseChangeMode(false);
//                            mHandler.sendEmptyMessage(1);
//                            a = true;
//                            break;
//                        }else {
//                            sensorHandlerCallback.updateSensorMatrix(temp);
//                            break;
//                        }
////                    }else {
////                        sensorHandlerCallback.updateSensorMatrix(rotationMatrix);
////                        break;
////                    }
//
////
////                    sensorHandlerCallback.updateSensorMatrix(rotationMatrix);
////                break;
//            }
//        }
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
