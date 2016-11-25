package de.inovex.fbuerkle.reliabilityexaminator.Handler;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by felix on 24/11/16.
 */

public class ProtocolHandler {

	private File angleLogfile;
	private File eventLogfile;
	private FileWriter angleFW;
	private FileWriter eventFW;

	private boolean active = false;

	public void startProtocol(String uuid){
		String path = "/storage/emulated/legacy/reliabilityexaminator/"
				+ new DateTime().toString("yyyyMMdd-HH:mm:ss") + "/";
		new File(path).mkdirs();
		angleLogfile = new File(path + "angles.csv");
		eventLogfile = new File(path + "adfEvents.csv");
		try {
			if(!angleLogfile.exists() || !angleLogfile.isFile()){
				angleLogfile.createNewFile();
			}
			angleFW = new FileWriter(angleLogfile, true);
			if(!eventLogfile.exists() || !eventLogfile.isFile()){
				eventLogfile.createNewFile();
			}
			eventFW = new FileWriter(eventLogfile, true);

//			angleFW.append("#" + new DateTime().toString("yyyyMMdd-HH:mm:ss") + "\t" + uuid + "\n");
//			eventFW.append("#" + new DateTime().toString("yyyyMMdd-HH:mm:ss") + "\t" + uuid + "\n");

			angleFW.append("#system-timestamp\tangle\n");
			eventFW.append("#system-timestamp\ttime since last event\tconfidence\n");
			active = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopProtocol(){
		active = false;
		try {
			if(angleFW != null){
				angleFW.flush();
				angleFW.close();
			}
			if(eventFW != null){
				eventFW.flush();
				eventFW.close();
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


}
