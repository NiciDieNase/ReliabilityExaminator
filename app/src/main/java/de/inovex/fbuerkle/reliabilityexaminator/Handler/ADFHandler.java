package de.inovex.fbuerkle.reliabilityexaminator.Handler;

import android.content.Context;
import android.widget.TextView;

import butterknife.BindView;
import de.inovex.fbuerkle.reliabilityexaminator.R;

/**
 * Created by felix on 22/11/16.
 */

public class ADFHandler {
	private final Context mContext;
	private final TangoHandler mTangoHandler;
	@BindView(R.id.tv_adf_status_value) protected TextView adfStatus;
	@BindView(R.id.tv_adf_name_value) protected TextView adfName;
	@BindView(R.id.tv_adf_id_value) protected TextView adfId;
	@BindView(R.id.tv_adf_located_value) protected TextView located;
	@BindView(R.id.tv_adf_lastlocated_value) protected TextView lastLocated;

	public ADFHandler(Context mContext, TangoHandler mTangoHandler) {
		this.mContext = mContext;
		this.mTangoHandler = mTangoHandler;
	}


}
