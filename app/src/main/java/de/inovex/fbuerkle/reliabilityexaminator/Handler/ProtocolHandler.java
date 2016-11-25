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

	private boolean active = false;

	public void startProtocol(String uuid){
		Log.d(TAG, "Start writing data-protocol");
		String path = "/storage/emulated/legacy/reliabilityexaminator/"
				+ new DateTime().toString("yyyyMMdd-HH:mm:ss") + "/";
		new File(path).mkdirs();
		File angleLogfile = new File(path + "angles.csv");
		File eventLogfile = new File(path + "adfEvents.csv");
		File positionLogfile = new File(path + "positions.csv");
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

//			angleFW.append("#" + new DateTime().toString("yyyyMMdd-HH:mm:ss") + "\t" + uuid + "\n");
//			eventFW.append("#" + new DateTime().toString("yyyyMMdd-HH:mm:ss") + "\t" + uuid + "\n");

			angleFW.append("#system-timestamp\tangle\n");
			eventFW.append("#system-timestamp\ttime since last event\tconfidence\n");
			positionFW.append("#system-timestamp\tx\ty\tz\n");
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

	public void logPosition(long systemTimestamp, double xPos, double yPos, double zPos){
		if(active){
			try {
				positionFW.append(String.format("%d\t%s\t%s\t%s\t\n",systemTimestamp,xPos,yPos,zPos));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void logADFLocationEvent(long systemTimestamp, double timeSinceLastEvent, int confidence, boolean isInitialLocation){
		if(active){
			try {
				if(isInitialLocation){
					eventFW.append(String.format("#Time to first Location: \t%s\n",timeSinceLastEvent));
				} else {
					eventFW.append(String.format("%d\t%s\t%d\n",systemTimestamp,timeSinceLastEvent,confidence));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
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
