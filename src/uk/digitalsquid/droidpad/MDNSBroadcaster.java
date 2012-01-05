package uk.digitalsquid.droidpad;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import uk.digitalsquid.ext.Base64;
import android.util.Log;

public class MDNSBroadcaster extends Thread implements LogTag {
	public final static String REMOTE_TYPE = "_droidpad._tcp.local.";
	
	private JmDNS jmdns;
	private ServiceInfo dpAnnounce;
	private final String devName, name;
	private final int port;
	
	public MDNSBroadcaster(String deviceName, int port) {
		devName = "droidpad:" + Base64.encodeBytes(deviceName.getBytes());
		this.name = deviceName;
		this.port = port;
	}
	
	private boolean stopping = false;
	
	@Override
	public void run() {
		while(jmdns == null) {
			if(stopping) return;
	        try {
				jmdns = JmDNS.create();
			} catch (IOException e) {
				Log.e(TAG, "Couldn't start JmDNS! Will try again soon.");
				e.printStackTrace();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		
        dpAnnounce = ServiceInfo.create(REMOTE_TYPE, devName, port, name);
        boolean registered = false;
        while(!registered) {
			if(stopping) {
				try {
					jmdns.unregisterAllServices();
					jmdns.close();
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
	        try {
				jmdns.registerService(dpAnnounce);
				registered = true;
			} catch (IOException e) {
				Log.e(TAG, "Couldn't register JmDNS! Will try again soon.");
				e.printStackTrace();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			if(stopping) {
				try {
					jmdns.unregisterAllServices();
					jmdns.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
        }
        while(!stopping) {
        	try {
        		synchronized(notifier) {
					notifier.wait();
        		}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        jmdns.unregisterAllServices();
        try {
			jmdns.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private final Object notifier = new Object();
	
	public void stopRunning() {
		stopping = true;
		synchronized(notifier) {
			notifier.notifyAll();
		}
	}
}
