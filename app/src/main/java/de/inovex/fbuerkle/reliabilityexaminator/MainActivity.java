package de.inovex.fbuerkle.reliabilityexaminator;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import de.inovex.fbuerkle.reliabilityexaminator.Sensors.PitchMeasurment;

public class MainActivity extends AppCompatActivity{

	private static final String TAG = MainActivity.class.getSimpleName();

	private boolean mIsConnected = false;
	private Tango mTango;
	private TangoConfig mConfig;

	private GLSurfaceView mTopPreview;
	private GLSurfaceView mBottomPreview;
	private HelloVideoRenderer mTopRenderer;
	private HelloVideoRenderer mBottomRenderer;
	private AtomicBoolean mIsRGBFrameAvailable = new AtomicBoolean(false);
	private AtomicBoolean mIsFisheyeFrameAvailable = new AtomicBoolean(false);
    private static final int INVALID_TEXTURE_ID = 0;
	private int mTopConnectedTextureIDGlThread = INVALID_TEXTURE_ID;
	private int mBottomConnectedTextureIDGlThread = INVALID_TEXTURE_ID;

	private PitchMeasurment mPitchMeasurment;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		mTopPreview = (GLSurfaceView) findViewById(R.id.top_preview);
		mBottomPreview = (GLSurfaceView) findViewById(R.id.bottom_preview);
		setupRenderer();

		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});
		TextView textView = (TextView) findViewById(R.id.tv_pitch_value);
		mPitchMeasurment = new PitchMeasurment(this,textView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mTopPreview.onResume();
		mBottomPreview.onResume();

		mTopPreview.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		mBottomPreview.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

		Log.d(TAG, "Resuming, starting Tango");
		mTango = new Tango(MainActivity.this, new Runnable() {
			@Override
			public void run() {
				synchronized (MainActivity.this){
					mConfig = setupTangoConfig(mTango);
					mTango.connect(mConfig);
					startupTango();
					mIsConnected = true;
					Log.d(TAG, "Resumed, Tango started");
				}
			}
		});

	mPitchMeasurment.registerListeners();
	}

	private TangoConfig setupTangoConfig(Tango tango) {
		TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
		config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);
		config.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);
		return config;
	}

	@Override
	protected void onPause() {
		super.onPause();
		mTopPreview.onPause();
		mBottomPreview.onPause();

		synchronized (this){
			try{
				mTango.disconnectCamera(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
				mTango.disconnectCamera(TangoCameraIntrinsics.TANGO_CAMERA_FISHEYE);
				mTopConnectedTextureIDGlThread = INVALID_TEXTURE_ID;
				mBottomConnectedTextureIDGlThread = INVALID_TEXTURE_ID;
				mTango.disconnect();
				mIsConnected = false;
			} catch (TangoErrorException e){
				Log.e(TAG, e.getMessage());
			}
		}
		mPitchMeasurment.unregisterListeners();
	}

	private void startupTango() {
		ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
		mTango.connectListener(framePairs, new Tango.OnTangoUpdateListener() {
			@Override
			public void onPoseAvailable(TangoPoseData tangoPoseData) {

			}

			@Override
			public void onXyzIjAvailable(TangoXyzIjData tangoXyzIjData) {

			}

			@Override
			public void onFrameAvailable(int cameraID) {
				if(cameraID == TangoCameraIntrinsics.TANGO_CAMERA_COLOR){
					Log.d(TAG, "New RGB-Frame");
					if(mTopPreview.getRenderMode() != GLSurfaceView.RENDERMODE_WHEN_DIRTY){
						mTopPreview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
					}
					mIsRGBFrameAvailable.set(true);
					mTopPreview.requestRender();
				}
				if(cameraID == TangoCameraIntrinsics.TANGO_CAMERA_FISHEYE){
					Log.d(TAG, "New Fisheye-Frame");
					if(mBottomPreview.getRenderMode() != GLSurfaceView.RENDERMODE_WHEN_DIRTY){
						mBottomPreview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
					}
					mIsFisheyeFrameAvailable.set(true);
					mBottomPreview.requestRender();
				}
			}

			@Override
			public void onTangoEvent(TangoEvent tangoEvent) {

			}

			@Override
			public void onPointCloudAvailable(TangoPointCloudData tangoPointCloudData) {

			}
		});
	}

	private void setupRenderer() {
		mTopPreview.setEGLContextClientVersion(2);
		mTopRenderer = new HelloVideoRenderer(new HelloVideoRenderer.RenderCallback() {
					@Override
					public void preRender() {
						if(!mIsConnected){
							Log.d(TAG,"Tango not connected, won't setup RGB-Renderer");
							return;
						}
						try{
							Log.d(TAG,"Tango connected, setting up RGB-Renderer");
							synchronized (MainActivity.this){
								if(mTopConnectedTextureIDGlThread == INVALID_TEXTURE_ID){
									mTopConnectedTextureIDGlThread = mTopRenderer.getTextureId();
									mTango.connectTextureId(TangoCameraIntrinsics.TANGO_CAMERA_COLOR,
											mTopRenderer.getTextureId());
								}
								if(mIsRGBFrameAvailable.compareAndSet(true,false)){
									double rgbTimestamp =
										mTango.updateTexture(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
								}
							}
						} catch (TangoErrorException e) {
							Log.e(TAG, "Tango API call error within the OpenGL thread", e);
						}
					}
				});
		mTopPreview.setRenderer(mTopRenderer);

		mBottomPreview.setEGLContextClientVersion(2);
		mBottomRenderer = new HelloVideoRenderer(new HelloVideoRenderer.RenderCallback() {

			@Override
			public void preRender() {
				if(!mIsConnected){
					Log.d(TAG,"Tango not connected, won't setup Fisheye Renderer");
					return;
				}
				try{
					Log.d(TAG,"Tango connected, setting up Fisheye-Renderer");
					synchronized (MainActivity.this){
						if(mBottomConnectedTextureIDGlThread == INVALID_TEXTURE_ID){
							mBottomConnectedTextureIDGlThread = mTopRenderer.getTextureId();
							mTango.connectTextureId(TangoCameraIntrinsics.TANGO_CAMERA_FISHEYE,
									mTopRenderer.getTextureId());
						}
						if(mIsFisheyeFrameAvailable.compareAndSet(true,false)){
							double rgbTimestamp =
									mTango.updateTexture(TangoCameraIntrinsics.TANGO_CAMERA_FISHEYE);
						}
					}
				} catch (TangoErrorException e) {
					Log.e(TAG, "Tango API call error within the OpenGL thread", e);
				}
			}
		});
		mBottomPreview.setRenderer(mBottomRenderer);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.action_settings:
				return true;
			case R.id.action_export_adf:
				// TODO export ADF
				return true;
			case R.id.action_load_adf:
				// TODO load ADF
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


}
