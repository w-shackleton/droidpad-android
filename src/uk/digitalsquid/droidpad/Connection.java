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

package uk.digitalsquid.droidpad;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import uk.digitalsquid.droidpad.Connection.Progress;
import uk.digitalsquid.droidpad.buttons.AnalogueData;
import uk.digitalsquid.droidpad.buttons.Button;
import uk.digitalsquid.droidpad.buttons.Item;
import uk.digitalsquid.droidpad.buttons.Slider;
import uk.digitalsquid.droidpad.serialise.BinarySerialiser;
import uk.digitalsquid.droidpad.serialise.ClassicSerialiser;
import android.os.AsyncTask;
import android.util.Log;

public class Connection extends AsyncTask<ConnectionInfo, Progress, Void> implements LogTag {
	
	public static final int STATE_CONNECTED = 1;
	public static final int STATE_WAITING = 2;
	public static final int STATE_CONNECTION_LOST = 3;
	
	public static final class Progress {
		public int status;
		public String connectedPc;
		
		public Progress(int status, String connectedPc) {
			this.status = status;
			this.connectedPc = connectedPc;
		}
	}
	
	private App app;
	
	private ConnectionInfo info;
	private boolean idling = true;
	
	private char[] inputTmpBuffer = new char[1024];
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
	}

	@Override
	protected Void doInBackground(ConnectionInfo... infos) {
		info = infos[0];
		Log.i(TAG, "Normal connection being created on " + info.port);
		ServerSocket serverSocket = createServerSocket(info.port, info.onlyBindLocalInsecure);
		if(serverSocket == null) return null;
		Log.i(TAG, "Created ServerSocket");
		
		app = (App) info.callbacks.getApplication();
		
		while(!isCancelled() && isRequired() && acceptSession(serverSocket));
		
		try {
			serverSocket.close();
		} catch (IOException e) { }
		
		Log.i(TAG, "Connection thread ending");
		
		return null;
	}
	
	private ServerSocket createServerSocket(int port, boolean onlyLocal) {
		ServerSocket ss = null;
		while(!isCancelled()) {
			try {
				if(onlyLocal) {
					Log.i(TAG, "Insecure connection only being created on local addresses");
					ss = new ServerSocket(port, 50, InetAddress.getByName("localhost"));
				} else 
					ss = new ServerSocket(port);
				ss.setSoTimeout(4000);
				return ss;
			} catch (IOException e) {
				Log.e(TAG, "Failed to create server socket", e);
				safeSleep(500);
			}
		}
		return null;
	}
	
	/**
	 * This is the main execution loop; it accepts a connection then uses it until
	 * disconnection
	 * @param serverSocket
	 * @return <code>false</code> if the thread should end now, <code>true</code>
	 * if another session should be accepted.
	 */
	@SuppressWarnings("resource")
	private boolean acceptSession(ServerSocket serverSocket) {
		idling = true;
		Socket socket = acceptConnection(serverSocket);
		if(socket == null) return true;
		Log.i(TAG, "Socket connection created");
		
		try {
			socket.setTcpNoDelay(true);
			socket.setKeepAlive(true);
		} catch (SocketException e) {
			Log.w(TAG, "Failed to set socket options", e);
		}
		
		BufferedOutputStream bufferedOutput = null;
		DataOutputStream dataOutput = null;
		InputStreamReader inputReader = null;
		try {
			bufferedOutput = new BufferedOutputStream(socket.getOutputStream());
			dataOutput = new DataOutputStream(bufferedOutput);
			inputReader = new InputStreamReader(socket.getInputStream());
		} catch (IOException e) {
			Log.e(TAG, "Failed to initialise IO streams", e);
			closeConnections(socket, inputReader, dataOutput, bufferedOutput);
			return true; // true means we want another connection
		}
		
		if(isCancelled()) return false;
		
		// Accumulate counts
		int numRawDevs = 1;
		int numAxes = 0;
		int numButtons = 0;
		for(Item item : info.spec.getLayout()) {
			if(item instanceof Slider) {
				Slider s = (Slider)item;
				switch(s.type) {
				case X:
				case Y:
					numAxes += 1; break;
				case Both:
					numAxes += 2; break;
				}
			} else if(item instanceof Button)
				numButtons++;
		}
		
		try {
			bufferedOutput.write(
					String.format("<MODE>%s</MODE><MODESPEC>%d,%d,%d</MODESPEC><SUPPORTSBINARY>\n",
					info.spec.getModeString(), numRawDevs, numAxes, numButtons).getBytes());
		} catch (IOException e) {
			Log.e(TAG, "Error sending info to computer", e);
			closeConnections(socket, inputReader, dataOutput, bufferedOutput);
			return true; // true means we want another connection
		}
		
		if(isCancelled()) return false;
		
		publishProgress(new Progress(STATE_CONNECTED, socket.getInetAddress().getHostAddress()));
		
		// Begin looping
		idling = false;
		boolean sendBinary = false;
		while(!isCancelled()) {
			AnalogueData analogue = new AnalogueData(
					info.callbacks.getAccelerometerValues(),
					info.callbacks.getGyroscopeValues(),
					info.callbacks.getWorldRotation(),
					info.reverseX, info.reverseY);
			try {
				if(sendBinary) {
					BinarySerialiser.writeBinary(dataOutput, analogue, info.callbacks.getScreenData());
				} else {
					bufferedOutput.write(ClassicSerialiser.formatLine(analogue, info.callbacks.getScreenData()).getBytes());
					bufferedOutput.flush();
				}
				
				// Reset button press overrides
				info.callbacks.getScreenData().resetOverrides();
				
				safeSleep((long)(info.interval * 1000L));
				
				if(inputReader.ready()) {
					int bytes = inputReader.read(inputTmpBuffer);
					if(bytes > 0) {
						String st = String.valueOf(inputTmpBuffer, 0, bytes);
						if(st.startsWith("<STOP>")) {
							publishProgress(new Progress(STATE_WAITING, ""));
							closeConnections(socket, inputReader, dataOutput, bufferedOutput);
							return false;
						} else if(st.startsWith("<BINARY>"))
							sendBinary = true;
					}
				}
				
			} catch (IOException e) {
				Log.w(TAG, "Lost connection with computer", e);
				closeConnections(socket, inputReader, dataOutput, bufferedOutput);
				publishProgress(new Progress(STATE_CONNECTION_LOST, ""));
				return true; // true means we want another connection
			}
		}
		// If we get to here, user must have cancelled the loop
		try {
			if(sendBinary) {
				BinarySerialiser.writeStopCommand(dataOutput);
			} else {
				bufferedOutput.write(ClassicSerialiser.writeStopCommand().getBytes());
				bufferedOutput.flush();
			}
			publishProgress(new Progress(STATE_WAITING, ""));
		} catch(IOException e) {
			Log.w(TAG, "Failed to send stop message to server", e);
		}
		closeConnections(socket, inputReader, dataOutput, bufferedOutput);
		
		return false;
	}
	
	private Socket acceptConnection(ServerSocket serverSocket) {
		int fb = 1;
		while(!isCancelled() && isRequired()) {
			try {
				return serverSocket.accept();
			} catch (IOException e) {
				String fbs = String.valueOf(fb);
				if(fb % 15 == 0)
					fbs = "FIZZBUZZ";
				else if(fb % 5 == 0)
					fbs = "BUZZ";
				else if(fb % 3 == 0)
					fbs = "FIZZ";
				Log.v(TAG, "Connection accept timed out, retrying... (" + fbs + " retries)");
				fb++;
			}
		}
		return null;
	}
	
	private void safeSleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) { }
	}
	
	private void closeConnections(Socket socket, Closeable... closeables) {
		closeConnections(closeables);
		try {
			socket.close();
		} catch (IOException e) {
			Log.w(TAG, "Failed to close socket", e);
		}
	}
	
	private void closeConnections(Closeable... closeables) {
		for(Closeable c : closeables) {
			try {
				if(c != null) c.close();
			} catch (IOException e) {
				Log.w(TAG, "Failed to close closeable", e);
			}
		}
	}
	
	@Override
	protected void onProgressUpdate(Progress... values) {
		super.onProgressUpdate(values);
		for(Progress p : values) {
			info.callbacks.broadcastState(p.status, p.connectedPc);
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		Log.d(TAG, "Loop finished successfully");
		info.callbacks.onConnectionFinished();
	}
	
	@Override
	protected void onCancelled() {
		super.onCancelled();
		Log.d(TAG, "Loop finished after being cancelled");
	}
	
	/**
	 * Closes the current connection if the thread is idling.
	 * @return <code>true</code> if the attempt succeeded, <code>false</code> otherwise.
	 */
	public boolean attemptClose() {
		if(idling) {
			this.cancel(true);
		} else {
			// Don't
		}
		return idling;
	}
	
	/**
	 * Returns <code>true</code> if the service is required by the UI
	 */
	protected boolean isRequired() {
		if(app != null) return app.isServiceRequired();
		return true;
	}
}
