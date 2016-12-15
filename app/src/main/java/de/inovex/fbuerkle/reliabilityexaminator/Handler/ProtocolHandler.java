package de.inovex.fbuerkle.reliabilityexaminator.Handler;

import android.support.design.widget.Snackbar;
import android.util.Log;

import com.google.atap.tangoservice.TangoPoseData;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by felix on 24/11/16.
 */

public class ProtocolHandler {

	private static final String TAG = ProtocolHandler.class.getSimpleName();
	public static final String TIMESTAMP = new DateTime().toString("yyyyMMdd-HH:mm:ss");
	public static final String STORAGE_PATH = "/storage/emulated/0/reliabilityexaminator/";
	private FileWriter angleFW;
	private FileWriter eventFW;
	private FileWriter positionFW;
	private FileWriter positionSoSFW;

	private boolean active = false;
	private String uuid;
	private String path;
	private boolean isInitialLocation = true;
	private long logStart;
	private TangoPoseData lastPose;

	public double getDistanceTraveled() {
		return distanceTraveled;
	}

	private double distanceTraveled = 0.0;

	public void startProtocol(String uuid){
		this.uuid = uuid;
		Log.d(TAG, "Start writing data-protocol");
		path = STORAGE_PATH
				+ TIMESTAMP + "/";
		new File(path).mkdirs();
		File angleLogfile = new File(path + "angles.csv");
		File eventLogfile = new File(path + "adfEvents.csv");
		File positionLogfile = new File(path + "positions.csv");
		File positionSosLogfile = new File(path + "positions_sos.csv");
		try {
			if(!angleLogfile.exists() || !angleLogfile.isFile()){
				angleLogfile.createNewFile();
			}
			angleFW = new FileWriter(angleLogfile, true);
			if(!eventLogfile.exists() || !eventLogfile.isFile()){
				eventLogfile.createNewFile();
			}
			eventFW = new FileWriter(eventLogfile, true);
			if(!positionLogfile.exists() || !positionLogfile.isFile()){
				positionLogfile.createNewFile();
			}
			positionFW = new FileWriter(positionLogfile, true);
			if(!positionSosLogfile.exists() || !positionSosLogfile.isFile()){
				positionSosLogfile.createNewFile();
			}
			positionSoSFW = new FileWriter(positionSosLogfile, true);

			angleFW.append(String.format("# ADF-ID: %s\n",this.uuid));
			angleFW.append("#system-timestamp\tangle\n");
			eventFW.append(String.format("# ADF-ID: %s\n",this.uuid));
			eventFW.append("#system-timestamp\ttime since last event\tconfidence\tx\ty\tz\tdistance\n");
			positionFW.append(String.format("# ADF-ID: %s\n",this.uuid));
			positionFW.append("#system-timestamp\tx\ty\tz\n");
			positionSoSFW.append(String.format("# ADF-ID: %s\n",this.uuid));
			positionSoSFW.append("#system-timestamp\tx\ty\tz\n");
			active = true;
		} catch (IOException e) {
			Snackbar.make(null,e.getMessage(),Snackbar.LENGTH_LONG).show();
		}
		logStart = System.currentTimeMillis();
	}

	public void stopProtocol(){
		flush();
		Log.d(TAG,"Stopping Protocol");
		if(active && isInitialLocation){
			Log.d(TAG,"Logging Localization Failure");
			long time = System.currentTimeMillis() - logStart;
			logLocalizationFailure(time);
		}
		active = false;
		try {
			if(angleFW != null){
				angleFW.close();
			}
			if(eventFW != null){
				eventFW.close();
			}
			if(positionFW != null){
				positionFW.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logDeviceAngle(long systemTimestamp, double angle){
		if(active){
			try {
				angleFW.append(String.format("%d\t%s\n", systemTimestamp, angle));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void logADFPosition(long systemTimestamp, double[] pos){
		if(active){
			try {
				positionFW.append(String.format("%d\t%s\t%s\t%s\t\n",systemTimestamp,pos[0],pos[1],pos[2]));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void logSoSPosition(long systemTimestamp, double[] pos){
		if(active){
			try {
				positionSoSFW.append(String.format("%d\t%s\t%s\t%s\t\n",systemTimestamp,pos[0],pos[1],pos[2]));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void logADFLocationEvent(long systemTimestamp, double timeSinceLastEvent, int confidence,
									double[] pos){
		if(active && timeSinceLastEvent != -1.0){
			try {
				if(!isInitialLocation){
					double dist = Math.sqrt(pos[0]*pos[0] + pos[1]*pos[1] + pos[2]*pos[2]);
					eventFW.append(String.format("%d\t%s\t%d\t%s\t%s\t%s\t%s\n",
							systemTimestamp,timeSinceLastEvent,confidence,pos[0],pos[1],pos[2],dist));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void logInitialLocalization(long timeToLocalization, long timestamp){
		try {
			File file = new File(STORAGE_PATH + "initialLocalization.csv");
			FileWriter writer = new FileWriter(file,true);
			if(!file.exists()){
				writer.append("# date\tsystem-timestamp\tadf-uuid\ttime-to-localization\n");
			}
			writer.append(String.format("%s\t%d\t%s\t%d\t%s\n",TIMESTAMP,timestamp,uuid,timeToLocalization,distanceTraveled));
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.isInitialLocation = false;
	}

	public void updateDistanceTraveled(TangoPoseData pose){
		if(pose.baseFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE
				&& pose.targetFrame == TangoPoseData.COORDINATE_FRAME_DEVICE){
			if(null != this.lastPose){
				double x = pose.translation[0] - lastPose.translation[0];
				double y = pose.translation[1] - lastPose.translation[1];
				double z = pose.translation[2] - lastPose.translation[2];
				double distance = Math.sqrt(x*x + y*y + z*z);
				this.distanceTraveled += distance;
			}
			this.lastPose = pose;
		}
	}

	private void logLocalizationFailure(long timeToFailure) {
		try {
			File file = new File(STORAGE_PATH + "localizationError.csv");
			FileWriter writer = new FileWriter(file, true);
			if(!file.exists()){
				writer.append("# date\tsystem-timestamp\tadf-uuid\ttimeToFailure\n");
			}
			if(distanceTraveled != 0.0){ // if distanceTraveld == 0.0 Tango service has failed/is not started
				writer.append(String.format("%s\t%s\t%d\t%s\n",TIMESTAMP,uuid,timeToFailure,distanceTraveled));
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void flush(){
		try {
			if(angleFW != null)
				angleFW.flush();
			if(eventFW != null)
				eventFW.flush();
			if(positionFW != null)
				positionFW.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
