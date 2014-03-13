package com.wenpin.sensor;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;

public class MainActivity extends Activity {
    private static final String TAG = "MySensor";

    private SurfaceView mSurfaceView = null;
    private SurfaceHolder mSurfaceHolder = null;
    private Paint mPaint = null;

    private SensorManager mSensorManager;
    private List<Sensor> mSensorList;
    private OnSensorEventListener mOnSensorEventListener =
        new OnSensorEventListener();
    
    private Timer mTimer;
    private TimerTask mTask;
    
    private int dataLen;
    
    private HashMap<Sensor, Integer> maps;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = (SurfaceView)findViewById(R.id.surfaceview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new MyHolder());

        mPaint = new Paint();
        mPaint.setStrokeWidth(1.0f);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        
        maps = new HashMap<Sensor, Integer>(mSensorList.size());
        
        for (Sensor sensor : mSensorList)
        	maps.put(sensor, 0);

        mTimer = new Timer(true);
    }

    private class OnSensorEventListener implements SensorEventListener {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.i(TAG, "accuracy changed to " + accuracy);
        }

        public void onSensorChanged(SensorEvent event) {
//        	Log.i(TAG, "onSensorChanged "
//        			+ "Sensor Name:" + event.sensor.getName() + " "
//					+ "Data len:" + event.values.length);
            dataLen += event.values.length;
            maps.put(event.sensor, maps.get(event.sensor) + event.values.length);
        }
    }

    public class MyHolder implements SurfaceHolder.Callback {
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.i(TAG, "surface changed to w:" + width + " h:" + height);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "surface created");
            for (Sensor sensor : mSensorList) {
            	Log.i(TAG, "GET sensor: " + sensor.getName() + "Vendor:" + sensor.getVendor());
            	mSensorManager.registerListener(mOnSensorEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
            }

            mTask = new MyTimerTask();
            mTimer.schedule(mTask, 0, 1000);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG, "surface destoryed");
         	Log.i(TAG, "Unregister sensor Listener");
            mSensorManager.unregisterListener(mOnSensorEventListener);
         	mTask.cancel();
        }
    }
    
    public class MyTimerTask extends TimerTask {
    	private final float X = 50;
    	private final float Y = 50;
    	@Override
    	public void run() {		
    		String dataRateText = "DATA Rate:" + dataLen * 4 + " Bytes/s";
            Canvas mCanvas = mSurfaceHolder.lockCanvas();

            try {
                if (mCanvas != null) {
                		mCanvas.drawColor(Color.BLACK);
                		mPaint.setColor(Color.GREEN);
                		mPaint.setTextSize(32.0f);
                        mCanvas.drawText(dataRateText, X, Y, mPaint);
                        int i = 0;
                        for (Sensor sensor : mSensorList) {
                        	i++;
                        	String sensorData = sensor.getName() + " " +
                        			maps.get(sensor) * 4 + " Bytes/s";
                        	mPaint.setColor(Color.BLUE);
                        	mCanvas.drawText(sensorData, X + 10, Y + i * 50, mPaint);
                        }
                    }
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCanvas != null) {
                    mSurfaceHolder.lockCanvas(new Rect(0, 0, 0, 0));
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            		dataLen = 0;
            		for (Sensor sensor : mSensorList)
            	        maps.put(sensor, 0);
                }
            }
    	}
    }
}
