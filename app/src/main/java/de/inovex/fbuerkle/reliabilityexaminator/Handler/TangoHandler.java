package de.inovex.fbuerkle.reliabilityexaminator.Handler;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.ViewGroup;

import com.google.atap.tango.ux.TangoUx;
import com.google.atap.tango.ux.TangoUxLayout;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoErrorException;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.inovex.fbuerkle.reliabilityexaminator.R;

/**
 * Created by felix on 22/11/16.
 */

public class TangoHandler {
	private static final String TAG = TangoHandler.class.getSimpleName();
	private final Context mContext;
	private final CameraHandler mCameraHandler;
	private final ADFHandler mADFHandler;
	private final TangoUx mTangoUx;

	private boolean mIsConnected = false;
	private Tango mTango;
	private TangoConfig mConfig;

	@BindView(R.id.layout_tango) TangoUxLayout mTangoUxLayout;
	@BindView(R.id.top_preview) GLSurfaceView rgbView;
	@BindView(R.id.bottom_preview) GLSurfaceView fisheyeView;

	public TangoHandler(Context context, ViewGroup view) {
		this.mContext = context;
		ButterKnife.bind(this, view);

		mTangoUx = new TangoUx(mContext);
		mTangoUx.setLayout(mTangoUxLayout);
		mCameraHandler = new CameraHandler(mContext,this,rgbView,fisheyeView);
		mADFHandler = new ADFHandler(mContext,this);
	}

	private TangoConfig setupTangoConfig(Tango tango) {
		return setupTangoConfig(tango,"");
	};

	private TangoConfig setupTangoConfig(Tango tango, String uuid) {
		TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
		config.putBoolean(TangoConfig.KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION, true);
		config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);
		config.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);
		config.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING,true);
		if(uuid != ""){
			config.putString(TangoConfig.KEY_STRING_AREADESCRIPTION,uuid);
		}
		return config;
	}

	public void onResume() {
		mCameraHandler.onResume();

		Log.d(TAG, "Resuming, starting Tango");
		final Runnable onTangoReady = new Runnable() {
			@Override
			public void run() {
				synchronized (mContext) {
					mConfig = setupTangoConfig(mTango);
					mTango.connect(mConfig);
					mCameraHandler.connectCamera();
					mIsConnected = true;
					Log.d(TAG, "Resumed, Tango started");
				}
			}
		};
		mTango =  new Tango(mContext,onTangoReady);
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

	public void exportADF(String uuid) {
		// TODO export ADF
		Log.d(TAG,"Exporting ADF " + uuid);
	}

	public void loadADF(String uuid) {
		// TODO load ADF
		synchronized (mContext){
			mCameraHandler.onPause();
			mTango.disconnect();
			mTango.connect(setupTangoConfig(mTango,uuid));
			mCameraHandler.onResume();
		}
	}
}
