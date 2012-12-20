/*  This file is part of DroidPad.
 *
 *  DroidPad is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DroidPad is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DroidPad.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.digitalsquid.droidpad2;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import uk.digitalsquid.droidpad2.buttons.AnalogueData;
import uk.digitalsquid.droidpad2.buttons.Button;
import uk.digitalsquid.droidpad2.buttons.Item;
import uk.digitalsquid.droidpad2.buttons.ModeSpec;
import uk.digitalsquid.droidpad2.buttons.Slider;
import uk.digitalsquid.droidpad2.serialise.BinarySerialiser;
import uk.digitalsquid.droidpad2.serialise.ClassicSerialiser;
import android.content.Intent;
import android.util.Log;

/**
 * This class actually opens up a connection and sends data.
 * @author william
 *
 */
public class OldConnection implements Runnable, LogTag {
	private App app;
	
	private ServerSocket ss;
	private Socket s;
	private OutputStream os;
	private InputStream is;
	private InputStreamReader isr;
	
	private BufferedOutputStream bufferedOutput;
	private DataOutputStream dataOutput;
	
	private int port = 0;
	/**
	 * Sending interval, in milliseconds.
	 */
	
	private int interval = 50;
	private ModeSpec mode;
	private DroidPadService parent;
	private boolean stopping = false;
	
	private int fb = 1;
	
	/**
	 * When true, binary data is sent.
	 */
	private boolean sendBinary = false;
	
	private boolean invX = false, invY = false;
	// NORMAL THREAD
	public OldConnection(DroidPadService droidPadService, int Port, int interval2, ModeSpec mode, boolean invX, boolean invY) {
		app = (App) droidPadService.getApplication();
		parent = droidPadService;
		port = Port;
		interval = (int) ((1 / (float) interval2) * 1000);
		
		this.invX = invX;
		this.invY = invY;
		
		this.mode = mode;
		//Toast.makeText(parent, String.valueOf(interval), Toast.LENGTH_SHORT).show();
		Log.v(TAG, "DPC: Infos recieved (initiated)");
	}
	
	@Override
	public void run() {
		Log.d(TAG, "DPC: Thread started. Waiting for connection...");
		server(false);
		if(!stopping) serverSetup();
		Log.d(TAG, "DPC: Someone has connected!");
		
		sendBinary = false;
		
		while(!stopping) {
			if(s != null) {
				try {
					AnalogueData analogue = new AnalogueData(parent.getAccelValues(), parent.getGyroValues(), invX, invY);
					
					if(!sendBinary) {
						String data = ClassicSerialiser.formatLine(analogue, parent.getButtons());
						
						bufferedOutput.write(data.getBytes());
						bufferedOutput.flush();
					} else {
						BinarySerialiser.writeBinary(dataOutput, analogue, parent.getButtons());
					}
					
					// Reset overridden button presses.
					parent.getButtons().resetOverrides();
					
					try {
						Thread.sleep(interval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(isr.ready())
					{
						char[] ch = new char[1024];
						isr.read(ch);
						String st = new String(ch);
						if(st.startsWith("<STOP>")) {
							parent.broadcastState(DroidPadService.STATE_WAITING, "");
							if(!app.isServiceRequired()) { // Free to close now.
								cleanup();
								Intent serviceIntent = new Intent(app, DroidPadService.class);
								app.stopService(serviceIntent);
								return;
							}
							server();
						} else if(st.startsWith("<BINARY>")) {
							// start sending binary packets instead
							sendBinary = true;
						}
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					if(!stopping) {
						Log.w(TAG, "Lost connection with computer.");
						parent.broadcastState(DroidPadService.STATE_CONNECTION_LOST, "");
						server();
						Log.d(TAG, "DPC: Someone else has connected.");
					}
				}
			}
		}
	}
	//END NORMAL THREAD
	
	//SETUP
	public final synchronized void killThread() {
		Log.d(TAG, "DPC: Thread dying...");
		if(!stopping) {
			stopping = true;
			
			try {
				if(bufferedOutput != null) {
					if(sendBinary) {
						BinarySerialiser.writeStopCommand(dataOutput);
					} else {
						bufferedOutput.write("<STOP>\n".getBytes()); // Doesn't really matter if this fails.
						bufferedOutput.flush();
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			cleanup();
		}
	}
	private void cleanup() {
		if(isr != null) {
			try {
				isr.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(is != null) {
			try {
				is.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(dataOutput != null) {
			try {
				dataOutput.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(bufferedOutput != null) {
			try {
				bufferedOutput.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(os != null) {
			try {
				os.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(s != null) {
			try {
				s.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(ss != null) {
			try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//END SETUP
	
	//SERVER METHODS
	private void server() { server(true); }
	private void server(boolean autoSetup) {
		// Layout not known now.
		parent.setCurrentLayout(null);
		serverInit();
		if(stopping) return; // Escape method
		serverMainLoop();
		if(!stopping) {
			try {
				s.setTcpNoDelay(true);
			} catch (SocketException e) {
				e.printStackTrace();
			}
			try {
				s.setKeepAlive(true);
			} catch (SocketException e) {
				e.printStackTrace();
			}
			if(autoSetup)serverSetup();
			parent.broadcastState(DroidPadService.STATE_CONNECTED, s.getInetAddress().getHostAddress());
		}
	}
	private void serverInit()
	{
		if(!stopping)
		{
			while(ss == null)
			{
				try {
					ss = new ServerSocket(port);
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "DPC: Couldn't initiate ServerSocket, perhaps not connected to network...");
					try
					{
						Thread.sleep(500);
					} catch (InterruptedException e1)
					{
						e1.printStackTrace();
					}
				}
				if(stopping) return;
			}
			try {
				ss.setSoTimeout(2000);
			} catch (SocketException e) {
				e.printStackTrace();
				Log.w(TAG, "DPC: Couldn't set timeout");
			}
		}
	}
	/**
	 * Wait for connections.
	 */
	private void serverMainLoop() {
		while(!stopping) {
			try {
				s = ss.accept();
				return;
			} catch (IOException e) {
				String fbs = String.valueOf(fb);
				if(fb % 15 == 0)
					fbs = "FIZZBUZZ";
				else if(fb % 5 == 0)
					fbs = "BUZZ";
				else if(fb % 3 == 0)
					fbs = "FIZZ";
				Log.v("DroidPad", "DPC: Timed out, retrying... (" + fbs + " retries)");
				fb++;
			}
		}
	}
	
	/**
	 * Opens streams once connected.
	 */
	private void serverSetup() {
		try {
			os = s.getOutputStream();
			bufferedOutput = new BufferedOutputStream(os);
			dataOutput = new DataOutputStream(bufferedOutput);
			sendBinary = false;
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "DPC: Couldn't create output stream");
		}
		try {
			is = s.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "DPC: Couldn't create input stream");
		}
		isr = new InputStreamReader(is);
		
		
		int numRawDevs = 1;
		int numAxes = 0;
		int numButtons = 0;
		for(Item item : mode.getLayout()) {
			if(item instanceof Slider) {
				Slider s = (Slider)item;
				switch(s.type) {
				case X:
				case Y:
					numAxes += 1;
					break;
				case Both:
					numAxes += 2;
					break;
				}
			} else if(item instanceof Button) {
				numButtons++;
			}
		}
		try {
			bufferedOutput.write(("<MODE>" + mode.getModeString() + "</MODE><MODESPEC>" + numRawDevs + "," + numAxes + "," + numButtons + "</MODESPEC><SUPPORTSBINARY>\n").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "DPC: Error sending info to PC");
		}
		
		// Set current layout in Service
		parent.setCurrentLayout(mode);
	}
}
