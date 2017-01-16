package de.inovex.fbuerkle.reliabilityexaminator.Handler;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.ViewGroup;
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
		// TODO handle Phab 2 Pro (Axes are different)
		mSensorManager.getRotationMatrix(mRotationMatrix,null,mAccelerometerReading,mMagnetometerReading);
		mSensorManager.getOrientation(mRotationMatrix,mOrientationAngles);
		double angle = mOrientationAngles[1] * -180 / Math.PI;

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
}
