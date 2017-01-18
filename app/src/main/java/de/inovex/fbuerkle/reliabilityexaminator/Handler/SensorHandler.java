package de.inovex.fbuerkle.reliabilityexaminator.Handler;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.inovex.fbuerkle.reliabilityexaminator.R;

/**
 * Created by felix on 22/11/16.
 */

public class SensorHandler implements SensorEventListener{
	private static final String TAG = SensorHandler.class.getSimpleName();
	private final Context mContext;

	private SensorManager mSensorManager;
	private Sensor mAccelerationSensor;
	private Sensor mMagnetometer;
	private final float[] mAccelerometerReading = new float[3];
	private final float[] mMagnetometerReading = new float[3];
	private final float[] mRotationMatrix = new float[9];
	private final float[] mOrientationAngles = new float[3];
	@BindView(R.id.tv_pitch_value) protected TextView tvPitchValue;
	private ProtocolHandler mProtocolHandler;

	public SensorHandler(Context context, ViewGroup rootView, ProtocolHandler mProtocolHandler){
		mContext = context;
		this.mProtocolHandler = mProtocolHandler;
		ButterKnife.bind(this,rootView);
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		mAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			System.arraycopy(sensorEvent.values,0,mAccelerometerReading,0,mAccelerometerReading.length);
		} else if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
			System.arraycopy(sensorEvent.values,0,mMagnetometerReading,0,mMagnetometerReading.length);
		}
		updateOrientationAngles();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {
		Log.d(TAG,"Sensor Accuracy Changed");
	}

	public void updateOrientationAngles(){
		mSensorManager.getRotationMatrix(mRotationMatrix,null,mAccelerometerReading,mMagnetometerReading);
		mSensorManager.getOrientation(mRotationMatrix,mOrientationAngles);
		double angle = -1.0;
		// Jellowstone has Landscape-default, Phab Portrait
		if(getDeviceDefaultOrientation() == Configuration.ORIENTATION_LANDSCAPE){
			angle = mOrientationAngles[1] * -180 / Math.PI;
		} else {
			angle = mOrientationAngles[2] * -180 / Math.PI;
		}

		double avg = mProtocolHandler.logDeviceAngle(System.currentTimeMillis(),angle);
		tvPitchValue.setText(String.format("%.1f°", avg));

	}

	public void onResume(){
		mSensorManager.registerListener(this,mAccelerationSensor,SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this,mMagnetometer,SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void onPause(){
		mSensorManager.unregisterListener(this);
	}

	private int getDeviceDefaultOrientation() {
		WindowManager windowManager =  (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

		Configuration config = mContext.getResources().getConfiguration();

		int rotation = windowManager.getDefaultDisplay().getRotation();

		if ( ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
				config.orientation == Configuration.ORIENTATION_LANDSCAPE)
			|| ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
				config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
		  return Configuration.ORIENTATION_LANDSCAPE;
		} else {
		  return Configuration.ORIENTATION_PORTRAIT;
		}
}
}
