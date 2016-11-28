package de.inovex.fbuerkle.reliabilityexaminator.Handler;

import android.util.Log;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by felix on 24/11/16.
 */

public class ProtocolHandler {

	private static final String TAG = ProtocolHandler.class.getSimpleName();
	private FileWriter angleFW;
	private FileWriter eventFW;
	private FileWriter positionFW;
	private FileWriter positionSoSFW;

	private boolean active = false;
	private String uuid;
	private String path;

	public void startProtocol(String uuid){
		this.uuid = uuid;
		Log.d(TAG, "Start writing data-protocol");
		path = "/storage/emulated/legacy/reliabilityexaminator/"
				+ new DateTime().toString("yyyyMMdd-HH:mm:ss") + "/";
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
			e.printStackTrace();
		}
	}

	public void stopProtocol(){
		flush();
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

	public void logADFPosition(long systemTimestamp, double xPos, double yPos, double zPos){
		if(active){
			try {
				positionFW.append(String.format("%d\t%s\t%s\t%s\t\n",systemTimestamp,xPos,yPos,zPos));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void logSoSPosition(long systemTimestamp, double xPos, double yPos, double zPos){
		if(active){
			try {
				positionSoSFW.append(String.format("%d\t%s\t%s\t%s\t\n",systemTimestamp,xPos,yPos,zPos));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void logADFLocationEvent(long systemTimestamp, double timeSinceLastEvent, int confidence, boolean isInitialLocation,float x,float y,float z){
		if(active){
			try {
				if(isInitialLocation){
					eventFW.append(String.format("#Time to first Location: \t%s\n",timeSinceLastEvent));
				} else {
					double dist = Math.sqrt(x*x + y*y + z*z);
					eventFW.append(String.format("%d\t%s\t%d\t%s\t%s\t%s\t%s\n",systemTimestamp,timeSinceLastEvent,confidence,x,y,z,dist));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void logInitialLocalization(long timeToLocalization, long timestamp){
		String date = new DateTime().toString("yyyyMMdd-HH:mm:ss");
		try {
			File file = new File("/storage/emulated/legacy/reliabilityexaminator/initialLocalization.csv");
			FileWriter writer = new FileWriter(file,true);
			if(!file.exists()){
				writer.append("# date\tsystem-timestamp\tadf-uuid\ttime-to-localization\n");
			}
			writer.append(String.format("%s\t%d\t%s\t%d\n",date,timestamp,uuid,timeToLocalization));
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
