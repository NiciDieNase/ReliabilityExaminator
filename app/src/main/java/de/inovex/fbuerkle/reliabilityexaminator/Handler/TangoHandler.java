package de.inovex.fbuerkle.reliabilityexaminator.Handler;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.atap.tango.ux.TangoUx;
import com.google.atap.tango.ux.TangoUxLayout;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import java.util.ArrayList;

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
	private ADFHandler mADFHandler;
	private final TangoUx mTangoUx;
	private ProtocolHandler mProtocolHandler;

	private boolean areaLearning;

	public String getUuid() {
		return uuid;
	}

	private ViewGroup rootView;

	private final String uuid;
	private boolean mIsConnected = false;

	private Tango mTango;
	private TangoConfig mConfig;
	@BindView(R.id.layout_tango) TangoUxLayout mTangoUxLayout;

	@BindView(R.id.top_preview) GLSurfaceView rgbView;
	@BindView(R.id.bottom_preview) GLSurfaceView fisheyeView;
	@BindView(R.id.tv_distance_value) TextView distance;
	public TangoHandler(Context context, ViewGroup view, String uuid, boolean areaLearning, ProtocolHandler mProtocolHandler) {
		this.mContext = context;
		rootView = view;
		this.uuid = uuid;
		this.mProtocolHandler = mProtocolHandler;
		ButterKnife.bind(this, view);
		this.areaLearning = areaLearning;

		mTangoUx = new TangoUx(mContext);
		mTangoUx.setLayout(mTangoUxLayout);
		mCameraHandler = new CameraHandler(mContext,this,rgbView,fisheyeView);
	}

	private void setupTangoListener() {
		Tango.OnTangoUpdateListener updateListener = new Tango.OnTangoUpdateListener() {

			@Override
			public void onPoseAvailable(TangoPoseData tangoPoseData) {
				if (null != mTangoUx) {
					mTangoUx.updatePoseStatus(tangoPoseData.statusCode);
				}
				mADFHandler.onPoseAvailable(tangoPoseData);
				mProtocolHandler.updateDistanceTraveled(tangoPoseData);
				((Activity)mContext).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						distance.setText(String.format("%.2f m",mProtocolHandler.getDistanceTraveled()));
					}
				});
			}

			@Override
			public void onXyzIjAvailable(TangoXyzIjData tangoXyzIjData) {
				if (mTangoUx != null) {
					mTangoUx.updateXyzCount(tangoXyzIjData.xyzCount);
				}
			}

			@Override
			public void onFrameAvailable(int i) {
				mCameraHandler.onFrameAvailable(i);
			}

			@Override
			public void onTangoEvent(TangoEvent tangoEvent) {
				if (null != mTangoUx) {
					mTangoUx.updateTangoEvent(tangoEvent);
				}
				if(tangoEvent.eventType == TangoEvent.EVENT_AREA_LEARNING
						&& tangoEvent.eventKey == TangoEvent.KEY_AREA_DESCRIPTION_SAVE_PROGRESS){
					Log.d(TAG, "Area Learning Update");
				}
			}

			@Override
			public void onPointCloudAvailable(TangoPointCloudData tangoPointCloudData) {

			}
		};
		ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
		framePairs.add(new TangoCoordinateFramePair(
				TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
				TangoPoseData.COORDINATE_FRAME_DEVICE));
		framePairs.add(new TangoCoordinateFramePair(
				TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
				TangoPoseData.COORDINATE_FRAME_DEVICE));
		framePairs.add(new TangoCoordinateFramePair(
				TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
				TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE));
		mTango.connectListener(framePairs,updateListener);

	}

	private TangoConfig setupTangoConfig(Tango tango) {
		TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
		config.putBoolean(TangoConfig.KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION, true);
		config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);
		config.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);
		config.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING,true);
		if(this.uuid != null && this.uuid != ""){
			config.putString(TangoConfig.KEY_STRING_AREADESCRIPTION,uuid);
			Log.d(TAG,"Loading ADF: " + uuid);
		}
		if(this.areaLearning){
			config.putBoolean(TangoConfig.KEY_BOOLEAN_LEARNINGMODE, true);
			Log.d(TAG,"Enabling Area Learning");
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
					setupTangoListener();
					TangoUx.StartParams params = new TangoUx.StartParams();
					params.showConnectionScreen = false;
					mTangoUx.start(params);
					try {
						mTango.connect(mConfig);
					} catch (TangoOutOfDateException e){
						if(null != mTangoUx){
							mTangoUx.showTangoOutOfDate();
						}
					} catch (TangoInvalidException e){
						Log.d(TAG, "TangoInvalidException" + e.getMessage());
					} catch (TangoErrorException e){
						e.printStackTrace();
					}
					mIsConnected = true;
					mCameraHandler.connectCamera();
					mADFHandler = new ADFHandler(mContext,TangoHandler.this,mProtocolHandler,rootView);
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
				mTangoUx.stop();
				mIsConnected = false;
			} catch (TangoErrorException e){
				Log.e(TAG, e.getMessage());
			}
		}
	}

	public boolean isConnected() {
		return mIsConnected;
	}


	public boolean isAreaLearning() {
		return areaLearning;
	}

	public Tango getTango() {
		return mTango;
	}

	public void exportADF(String uuid) {
		Log.d(TAG,"Exporting ADF " + uuid);
		String exportDir = "/storage/emulated/legacy/TangoADFs/";
		mTango.exportAreaDescriptionFile(uuid,exportDir);
	}

	public void takeScreenshot(){
		mCameraHandler.takeScreenshots();
	}

	public String generateADFName(){
		// Build String
		StringBuilder builder = new StringBuilder();
		builder.append(mProtocolHandler.TIMESTAMP).append(" ");
		builder.append(mProtocolHandler.getDistanceTraveled());
		if(uuid != ""){
			builder.append(" ").append("extends ").append(uuid);
		}
		return builder.toString();
	}

	public void saveADF() {
		mADFHandler.saveADF();
	}
}
