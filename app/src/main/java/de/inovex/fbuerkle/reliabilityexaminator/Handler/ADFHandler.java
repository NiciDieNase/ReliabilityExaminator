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
	@BindView(R.id.tv_adf_status_value) protected TextView adfStatus;
	@BindView(R.id.tv_adf_name_value) protected TextView adfName;
	@BindView(R.id.tv_adf_id_value) protected TextView adfId;
	@BindView(R.id.tv_adf_located_value) protected TextView located;
	@BindView(R.id.tv_adf_lastlocated_value) protected TextView lastLocated;
	@BindView(R.id.tv_adf_confidence_value) protected TextView confidence;
	private double lastLocatedTime;

	public ADFHandler(final Context mContext, final TangoHandler mTangoHandler, final ViewGroup rootView) {
		this.mContext = mContext;
		this.mTangoHandler = mTangoHandler;

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
					if(pose.statusCode == TangoPoseData.POSE_VALID){
						located.setText(R.string.yes);
					}
					confidence.setText(Integer.toString(pose.confidence));
					lastLocatedTime = pose.timestamp;
					lastLocated.setText(Double.toString(lastLocatedTime));
				}
			});
		}
	}


}
