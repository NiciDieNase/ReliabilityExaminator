package de.inovex.fbuerkle.reliabilityexaminator.Handler;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.atap.tangoservice.TangoAreaDescriptionMetaData;
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
	private final long mStartTime;
	private boolean mADFLocated = false;
	@BindView(R.id.tv_adf_status_value) TextView adfStatus;
	@BindView(R.id.tv_adf_name_value) TextView adfName;
	@BindView(R.id.tv_adf_id_value) TextView adfId;
	@BindView(R.id.tv_adf_located_value) TextView located;
	@BindView(R.id.tv_adf_lastlocated_value) TextView lastLocated;
	@BindView(R.id.tv_adf_confidence_value) TextView confidence;
	@BindView(R.id.tv_adf_located_time) TextView adfTime;
	private double lastLocatedTime = 0.0;

	public ADFHandler(final Context mContext, final TangoHandler mTangoHandler, final ViewGroup rootView) {
		this.mContext = mContext;
		this.mTangoHandler = mTangoHandler;
		mStartTime = System.currentTimeMillis();

		final String uuid = mTangoHandler.getUuid();
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
					if(pose.statusCode == TangoPoseData.POSE_VALID && mADFLocated){
						located.setText(R.string.yes);
						long time = System.currentTimeMillis() - mStartTime;
						adfTime.setText(Long.toString(time) + " ms");
					}
					confidence.setText(Integer.toString(pose.confidence));
					lastLocated.setText(Double.toString(pose.timestamp-lastLocatedTime));
					lastLocatedTime = pose.timestamp;
				}
			});
		}
	}


}
