package de.inovex.fbuerkle.reliabilityexaminator.Handler;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoAreaDescriptionMetaData;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoPoseData;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.inovex.fbuerkle.reliabilityexaminator.R;

/**
 * Created by felix on 22/11/16.
 */

public class ADFHandler {
	private final String TAG = ADFHandler.class.getSimpleName();

	private final Context mContext;
	private final TangoHandler mTangoHandler;
	private final ProtocolHandler mProtocolHandler;
	private final long mStartTime;
	private final String uuid;
	private boolean mADFLocated = false;
	@BindView(R.id.tv_adf_status_value) TextView adfStatus;
	@BindView(R.id.tv_adf_name_value) TextView adfName;
	@BindView(R.id.tv_adf_id_value) TextView adfId;
	@BindView(R.id.tv_adf_located_value) TextView located;
	@BindView(R.id.tv_adf_lastlocated_value) TextView lastLocated;
	@BindView(R.id.tv_adf_located_time) TextView adfTime;
	private double lastEventTimestamp = -1.0;

	public ADFHandler(final Context mContext, final TangoHandler mTangoHandler,
					  final ProtocolHandler mProtocolHandler, final ViewGroup rootView) {
		this.mContext = mContext;
		this.mTangoHandler = mTangoHandler;
		this.mProtocolHandler = mProtocolHandler;
		mStartTime = System.currentTimeMillis();

		uuid = mTangoHandler.getUuid();
		Log.d(TAG,uuid != null ? uuid : "No ADF");
		if(uuid != null && uuid != ""){
			((Activity)mContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ButterKnife.bind(ADFHandler.this,rootView);
					adfStatus.setText(R.string.yes);
					adfId.setText(uuid);
					byte[] bytes = mTangoHandler.getTango()
							.loadAreaDescriptionMetaData(uuid).get(TangoAreaDescriptionMetaData.KEY_NAME);
					String name = bytes != null ? new String(bytes) : "<no_name_found>";
					adfName.setText(name);
				}
			});
		}
		Log.d(TAG,"ADF Handler Started");
	}

	public void onPoseAvailable(final TangoPoseData pose){
		if(pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION
				&& pose.targetFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE){
			((Activity)mContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(pose.statusCode == TangoPoseData.POSE_VALID){
						double timeSinceLastEvent = -1.0;
						if(lastEventTimestamp != -1.0){
							timeSinceLastEvent = pose.timestamp-lastEventTimestamp;
						}
						long timestamp = System.currentTimeMillis();
						if(!mADFLocated && "" != uuid){
							located.setText(R.string.yes);
							long time = timestamp - mStartTime;
							adfTime.setText(Long.toString(time) + " ms");
							mProtocolHandler.logInitialLocalization(time,timestamp);
							mADFLocated = true;
						}
						double[] position = pose.translation;

						// Get Device Position in ADF-Coordinates to calculate diff
						try{

							TangoPoseData adfPose = mTangoHandler.getTango().getPoseAtTime(pose.timestamp,
									Tango.COORDINATE_FRAME_ID_AREA_DESCRIPTION, Tango.COORDINATE_FRAME_ID_DEVICE);
							double[] pos = adfPose.translation.clone();
							pos[0]-=position[0];
							pos[1]-=position[1];
							pos[2]-=position[2];
						} catch (TangoErrorException e){
							e.printStackTrace();
						}

						mProtocolHandler.logADFLocationEvent(timestamp,
								timeSinceLastEvent,pose.confidence,
								position);
						if("" != uuid){
							lastLocated.setText(Double.toString(timeSinceLastEvent));
						}
						lastEventTimestamp = pose.timestamp;
					}
				}
			});
		}
		if(pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION
				&& pose.targetFrame == TangoPoseData.COORDINATE_FRAME_DEVICE){
			mProtocolHandler.logADFPosition(System.currentTimeMillis(),pose.translation);
		}
		if(pose.baseFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE
				&& pose.targetFrame == TangoPoseData.COORDINATE_FRAME_DEVICE){
			mProtocolHandler.logSoSPosition(System.currentTimeMillis(),pose.translation);
		}
	}


}
