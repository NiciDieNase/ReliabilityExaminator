package de.inovex.fbuerkle.reliabilityexaminator.Handler;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoErrorException;

/**
 * Created by felix on 22/11/16.
 */

public class TangoHandler {
	private static final String TAG = TangoHandler.class.getSimpleName();
	private final Context mContext;
	private final CameraHandler mCameraHandler;
	private final ADFHandler mADFHandler;

	private boolean mIsConnected = false;
	private Tango mTango;
	private TangoConfig mConfig;

	public TangoHandler(Context context, GLSurfaceView rgbView, GLSurfaceView fisheyeView) {
		this.mContext = context;

		mCameraHandler = new CameraHandler(mContext,this,rgbView,fisheyeView);
		mADFHandler = new ADFHandler(mContext,this);
	}

	private TangoConfig setupTangoConfig(Tango tango) {
		TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
		config.putBoolean(TangoConfig.KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION, true);
		config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);
		config.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);


		return config;
	}

	public void onResume() {
		mCameraHandler.onResume();

		Log.d(TAG, "Resuming, starting Tango");
		mTango = new Tango(mContext, new Runnable() {
			@Override
			public void run() {
				synchronized (mContext){
					mConfig = setupTangoConfig(mTango);
					mTango.connect(mConfig);
					mCameraHandler.connectCamera();
					mIsConnected = true;
					Log.d(TAG, "Resumed, Tango started");
				}
			}
		});
	}

	public void onPause() {
		mCameraHandler.onPause();

		synchronized (mContext){
			try{
				mCameraHandler.disconnectCamera();
				mTango.disconnect();
				mIsConnected = false;
			} catch (TangoErrorException e){
				Log.e(TAG, e.getMessage());
			}
		}
	}


	public boolean isConnected() {
		return mIsConnected;
	}

	public Tango getTango() {
		return mTango;
	}
}
