package de.inovex.fbuerkle.reliabilityexaminator.Handler;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoInvalidException;

import java.util.concurrent.atomic.AtomicBoolean;

import de.inovex.fbuerkle.reliabilityexaminator.HelloVideoRenderer;

/**
 * Created by felix on 22/11/16.
 */

public class CameraHandler {
	private static final String TAG = CameraHandler.class.getSimpleName();
	private final Context mContext;
	private TangoHandler mTangoHandler;
	private final GLSurfaceView mRgbView;
	private final GLSurfaceView mFisheyeView;
	private HelloVideoRenderer mRgbRenderer;
	private HelloVideoRenderer mFisheyeRenderer;

	private AtomicBoolean mIsRGBFrameAvailable = new AtomicBoolean(false);
	private AtomicBoolean mIsFisheyeFrameAvailable = new AtomicBoolean(false);
	private static final int INVALID_TEXTURE_ID = 0;
	private int mTopConnectedTextureIDGlThread = INVALID_TEXTURE_ID;
	private int mBottomConnectedTextureIDGlThread = INVALID_TEXTURE_ID;
	private boolean isConnected = false;

	public CameraHandler(Context mContext,TangoHandler mTangoHandler, GLSurfaceView mRgbView, GLSurfaceView mFisheyeView){
		this.mContext = mContext;
		this.mTangoHandler = mTangoHandler;
		this.mRgbView = mRgbView;
		this.mFisheyeView = mFisheyeView;

		setupRenderer();
	}

	public void onPause() {
		mRgbView.onPause();
		mFisheyeView.onPause();
	}

	public void onResume() {
		//Camera
		mRgbView.onResume();
		mFisheyeView.onResume();
		mRgbView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		mFisheyeView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}

	private void setupRenderer() {
		mRgbView.setEGLContextClientVersion(2);
		mRgbRenderer = new HelloVideoRenderer(new HelloVideoRenderer.RenderCallback() {
			@Override
			public void preRender() {
				if(!mTangoHandler.isConnected()){
					Log.d(TAG,"Tango not connected, won't setup RGB-Renderer");
					return;
				}
				try{
//					Log.d(TAG,"Tango connected, setting up RGB-Renderer");
					synchronized (mContext){
						try{
							if(mTopConnectedTextureIDGlThread == INVALID_TEXTURE_ID){
								mTopConnectedTextureIDGlThread = mRgbRenderer.getTextureId();
								mTangoHandler.getTango().connectTextureId(TangoCameraIntrinsics.TANGO_CAMERA_COLOR,
										mRgbRenderer.getTextureId());
							}
							if(mIsRGBFrameAvailable.compareAndSet(true,false)){
								double rgbTimestamp =
										mTangoHandler.getTango().updateTexture(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
							}
						} catch (TangoInvalidException e){
							Log.d(TAG,e.getMessage());
						} catch (NullPointerException e){
							Log.d(TAG,e.getMessage());
						}
					}
				} catch (TangoErrorException e) {
					Log.e(TAG, "Tango API call error within the OpenGL thread", e);
				}
			}
		});
		mRgbView.setRenderer(mRgbRenderer);

		mFisheyeView.setEGLContextClientVersion(2);
		mFisheyeRenderer = new HelloVideoRenderer(new HelloVideoRenderer.RenderCallback() {

			@Override
			public void preRender() {
				if(!mTangoHandler.isConnected()){
					Log.d(TAG,"Tango not connected, won't setup Fisheye Renderer");
					return;
				}
				try{
//					Log.d(TAG,"Tango connected, setting up Fisheye-Renderer");
					synchronized (mContext){
						if(mBottomConnectedTextureIDGlThread == INVALID_TEXTURE_ID){
							mBottomConnectedTextureIDGlThread = mRgbRenderer.getTextureId();
							mTangoHandler.getTango().connectTextureId(TangoCameraIntrinsics.TANGO_CAMERA_FISHEYE,
									mRgbRenderer.getTextureId());
						}
						if(mIsFisheyeFrameAvailable.compareAndSet(true,false)){
							double rgbTimestamp =
									mTangoHandler.getTango().updateTexture(TangoCameraIntrinsics.TANGO_CAMERA_FISHEYE);
						}
					}
				} catch (TangoErrorException e) {
					Log.e(TAG, "Tango API call error within the OpenGL thread", e);
				}
			}
		});
		mFisheyeView.setRenderer(mFisheyeRenderer);
	}

	public void connectCamera() {
		this.isConnected = true;
	}

	public void disconnectCamera() {
		this.isConnected = false;
		mTangoHandler.getTango().disconnectCamera(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
		mTangoHandler.getTango().disconnectCamera(TangoCameraIntrinsics.TANGO_CAMERA_FISHEYE);
		mTopConnectedTextureIDGlThread = INVALID_TEXTURE_ID;
		mBottomConnectedTextureIDGlThread = INVALID_TEXTURE_ID;
	}

	public void onFrameAvailable(int cameraID) {
		if(isConnected){
			if(cameraID == TangoCameraIntrinsics.TANGO_CAMERA_COLOR){
//					Log.d(TAG, "New RGB-Frame");
				if(mRgbView.getRenderMode() != GLSurfaceView.RENDERMODE_WHEN_DIRTY){
					mRgbView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
				}
				mIsRGBFrameAvailable.set(true);
				mRgbView.requestRender();
			}
			if(cameraID == TangoCameraIntrinsics.TANGO_CAMERA_FISHEYE){
//					Log.d(TAG, "New Fisheye-Frame");
				if(mFisheyeView.getRenderMode() != GLSurfaceView.RENDERMODE_WHEN_DIRTY){
					mFisheyeView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
				}
				mIsFisheyeFrameAvailable.set(true);
				mFisheyeView.requestRender();
			}
		}
	}
}
