package de.inovex.fbuerkle.reliabilityexaminator.Handler;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoAreaDescriptionMetaData;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import java.util.ArrayList;

import butterknife.BindView;
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

	public ADFHandler(Context mContext, TangoHandler mTangoHandler) {
		this.mContext = mContext;
		this.mTangoHandler = mTangoHandler;

		String uuid = mTangoHandler.getUuid();
		if(uuid != null && uuid != ""){
			adfStatus.setText(R.string.yes);
			adfId.setText(uuid);
			byte[] bytes = mTangoHandler.getTango()
					.loadAreaDescriptionMetaData(uuid).get(TangoAreaDescriptionMetaData.KEY_NAME);
			String name = bytes != null ? new String(bytes) : "<no_name_found>";
			adfName.setText(name);
		}
		ArrayList<TangoCoordinateFramePair> tangoCoordinateFramePairs = new ArrayList<>();
		TangoCoordinateFramePair pair = new TangoCoordinateFramePair();
		pair.baseFrame = TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION;
		pair.targetFrame = TangoPoseData.COORDINATE_FRAME_DEVICE;
		tangoCoordinateFramePairs.add(pair);
		mTangoHandler.getTango().connectListener(tangoCoordinateFramePairs, new Tango.OnTangoUpdateListener() {
			@Override
			public void onPoseAvailable(TangoPoseData tangoPoseData) {
				confidence.setText(tangoPoseData.confidence);
				lastLocatedTime = tangoPoseData.timestamp;
				lastLocated.setText(Double.toString(lastLocatedTime));
			}

			@Override
			public void onXyzIjAvailable(TangoXyzIjData tangoXyzIjData) {

			}

			@Override
			public void onFrameAvailable(int i) {

			}

			@Override
			public void onTangoEvent(TangoEvent tangoEvent) {

			}

			@Override
			public void onPointCloudAvailable(TangoPointCloudData tangoPointCloudData) {

			}
		});
		Log.d(TAG,"ADF Handler Started");
	}


}
