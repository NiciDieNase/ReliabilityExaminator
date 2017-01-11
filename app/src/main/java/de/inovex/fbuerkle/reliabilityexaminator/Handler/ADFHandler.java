package de.inovex.fbuerkle.reliabilityexaminator.Handler;

import android.app.Activity;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoAreaDescriptionMetaData;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoException;
import com.google.atap.tangoservice.TangoPoseData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.inovex.fbuerkle.reliabilityexaminator.ADFDataSource;
import de.inovex.fbuerkle.reliabilityexaminator.Activities.ExaminatorActivity;
import de.inovex.fbuerkle.reliabilityexaminator.R;

/**
 * Created by felix on 22/11/16.
 */

public class ADFHandler {
	private final String TAG = ADFHandler.class.getSimpleName();

	private final Context mContext;
	private final TangoHandler mTangoHandler;
	private final ProtocolHandler mProtocolHandler;
	private ViewGroup rootView;
	private final long mStartTime;
	private final String uuid;
	private boolean mADFLocated = false;
	@BindView(R.id.tv_adf_status_value) TextView adfStatus;
	@BindView(R.id.tv_adf_name_value) TextView adfName;
	@BindView(R.id.tv_adf_id_value) TextView adfId;
	@BindView(R.id.tv_adf_located_value) TextView located;
	@BindView(R.id.tv_adf_lastlocated_value) TextView lastLocated;
	@BindView(R.id.tv_adf_located_time) TextView adfTime;
	@BindView(R.id.fab_save) FloatingActionButton fabSave;
	private double lastEventTimestamp = -1.0;

	public ADFHandler(final Context mContext, final TangoHandler mTangoHandler,
					  final ProtocolHandler mProtocolHandler, final ViewGroup rootView) {
		this.mContext = mContext;
		this.mTangoHandler = mTangoHandler;
		this.mProtocolHandler = mProtocolHandler;
		this.rootView = rootView;
		mStartTime = System.currentTimeMillis();

		uuid = mTangoHandler.getUuid();
		Log.d(TAG,uuid != null ? uuid : "No ADF");
		((Activity)mContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ButterKnife.bind(ADFHandler.this, rootView);
				if (uuid != null && uuid != "") {
					adfStatus.setText(R.string.adf_status_loaded);
					adfId.setText(uuid);
					byte[] bytes = mTangoHandler.getTango()
							.loadAreaDescriptionMetaData(uuid).get(TangoAreaDescriptionMetaData.KEY_NAME);
					String name = bytes != null ? new String(bytes) : "<no_name_found>";
					adfName.setText(name);
				}
				if(mTangoHandler.isAreaLearning() && adfStatus != null){
					if(uuid == ""){
						adfStatus.setText(R.string.adf_status_learning);
						fabSave.show();
					} else {
						adfStatus.setText(R.string.adf_status_extending);
					}
				} else {
					if (uuid == ""){
						adfStatus.setText(R.string.adf_status_noadf);
					} else {
						adfStatus.setText(R.string.adf_status_loaded);
					}
				}
			}
		});
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
							mADFLocated = true;
							if(mTangoHandler.isAreaLearning()){
								fabSave.show();
							} else {
								mProtocolHandler.logInitialLocalization(time,timestamp,pose.translation);

							}
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

	public void saveAllAdfNames(){
		new AsyncTask<Void, Long, Void>() {
			@Override
			protected Void doInBackground(Void... voids) {
				ADFDataSource adfDataSource = new ADFDataSource(mContext, mTangoHandler.getTango());
				Map<String, String> adfMap = adfDataSource.getUUIDMap();
				File adfList = new File("/storage/emulated/legacy/reliabilityexaminator/adfList.csv");
				try {
					if(!adfList.exists() || !adfList.isFile()){
						adfList.createNewFile();
					}
					FileWriter writer = new FileWriter(adfList);
					writer.append("uuid\tADF-Name\n");
					for(Map.Entry<String,String> e : adfMap.entrySet()){
						String string = String.format("%s\t%s\n", e.getKey(), e.getValue());
						Log.d(TAG,string);
						writer.append(string);
					}
					writer.flush();
					writer.close();
					MediaScannerConnection.scanFile(mContext, new String[] { adfList.getAbsolutePath() }, null, null);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
	}

	public void saveADF(String comment) {
		if(mTangoHandler.isAreaLearning()){
			new AsyncTask<String,Long,String>(){
				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					((ExaminatorActivity)mContext).showLoadingDialog();
					adfStatus.setText(R.string.adf_status_saving);
				}

				@Override
				protected void onPostExecute(String s) {
					super.onPostExecute(s);
					((ExaminatorActivity)mContext).hideLoadingDialog();
				}

				@Override
				protected String doInBackground(String... objectInputs) {
					String newUUID = "";
					try{
						String comment = objectInputs[0];
						Tango tango = mTangoHandler.getTango();
						newUUID = tango.saveAreaDescription();
						// name ADF
						String adfInfo = mTangoHandler.generateADFName() + "\n" + comment;
						TangoAreaDescriptionMetaData metadata;
						metadata = tango.loadAreaDescriptionMetaData(newUUID);
						metadata.set(TangoAreaDescriptionMetaData.KEY_NAME, adfInfo.getBytes());
						tango.saveAreaDescriptionMetadata(newUUID, metadata);
						File metaFile = new File("/storage/emulated/legacy/reliabilityexaminator/adfMetadata.csv");
						FileWriter metaDataWriter = new FileWriter(metaFile,true);
//						if(!metaFile.exists() || !metaFile.isFile()){
//							metaFile.createNewFile();
//							Log.d(TAG,"Writing new ADF-Metadatafile");
//						}
//						if(metaFile.length() < 1){
//							metaDataWriter.write("# uuid\ttimestamp\tdistance\textends\n");
//							Log.d(TAG,"Writing ADF-Log Header");
//						}
						String log = mTangoHandler.generateLogString();
						metaDataWriter.append(newUUID).append("\t").append(log);
						metaDataWriter.flush();
						metaDataWriter.close();
						Snackbar.make(rootView,"Saved ADF "+ log.replace("\t"," ") + " " + newUUID,Snackbar.LENGTH_SHORT).show();
					} catch (TangoException e){
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						((ExaminatorActivity)mContext).loadADF(newUUID);
					}
					return newUUID;
				}
			}.execute(comment);

		}
	}
}
