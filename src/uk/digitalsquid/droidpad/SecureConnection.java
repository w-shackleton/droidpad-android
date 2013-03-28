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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.spongycastle.crypto.tls.PSKTlsClient;
import org.spongycastle.crypto.tls.TlsProtocolHandler;

import uk.digitalsquid.droidpad.SecureConnection.Progress;
import uk.digitalsquid.droidpad.buttons.AnalogueData;
import uk.digitalsquid.droidpad.buttons.Button;
import uk.digitalsquid.droidpad.buttons.Item;
import uk.digitalsquid.droidpad.buttons.Slider;
import uk.digitalsquid.droidpad.serialise.BinarySerialiser;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

/**
 * This is an improved version of the {@link Connection} class. It enforces encryption to be used,
 * operates on a different port and only uses the new (binary) protocol. The old
 * {@link Connection} class is to be phased out in the future.
 * @author william
 *
 */
public class SecureConnection extends AsyncTask<ConnectionInfo, Progress, Void> implements LogTag {
	
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
	
	/**
	 * A message from the client
	 * @author william
	 *
	 */
	static class ClientMessage {
		public static final int CMD_STOP = 1;
		int what;
		public ClientMessage(int what) {
			this.what = what;
		}
	}
	private ConcurrentLinkedQueue<ClientMessage> clientMessages;
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
	}

	@Override
	protected Void doInBackground(ConnectionInfo... infos) {
		info = infos[0];
		ServerSocket serverSocket = createServerSocket(info.securePort);
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
	
	private ServerSocket createServerSocket(int port) {
		ServerSocket ss = null;
		while(!isCancelled()) {
			try {
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
	@SuppressLint("NewApi")
	private boolean acceptSession(ServerSocket serverSocket) {
		idling = true;
		Socket socket = acceptConnection(serverSocket);
		TlsProtocolHandler protocol = null;
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
		InputStream innerInput = null;
		try {
			// We are always encrypting data here
			Log.v(TAG, "Setting up SSL connection");
			protocol = new TlsProtocolHandler(socket.getInputStream(), socket.getOutputStream());
			PSKTlsClient tlsClient = new PSKTlsClient(info.identity);
			try {
				Log.v(TAG, "Attempting handshake");
				protocol.connect(tlsClient);
				Log.v(TAG, "Handshake completed");
			} catch(IOException e) {
				// Failed to complete handshake
				Log.e(TAG, "Failed to complete handshake", e);
				info.callbacks.broadcastAlert(ConnectionCallbacks.ALERT_AUTH_FAILED);
				closeConnections(socket, protocol, dataOutput, bufferedOutput);
				return true;
			}
			
			OutputStream innerOutput = protocol.getOutputStream();
			innerInput = protocol.getInputStream();
			
			bufferedOutput = new BufferedOutputStream(innerOutput);
			dataOutput = new DataOutputStream(bufferedOutput);
			
		} catch (IOException e) {
			Log.e(TAG, "Failed to initialise IO streams", e);
			closeConnections(socket, protocol, dataOutput, bufferedOutput);
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
			BinarySerialiser.writeConnectionInfo(dataOutput, info.spec.getMode(), numRawDevs, numAxes, numButtons);
		} catch (IOException e) {
			Log.e(TAG, "Error sending info to computer", e);
			closeConnections(socket, protocol, dataOutput, bufferedOutput);
			return true; // true means we want another connection
		}
		
		if(isCancelled()) return false;
		
		publishProgress(new Progress(STATE_CONNECTED, socket.getInetAddress().getHostAddress()));
		
		// Start client response listener
		clientMessages = new ConcurrentLinkedQueue<SecureConnection.ClientMessage>();
		ResponseListener responseListener = new ResponseListener();
		// Android AsyncTask version weirdness
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			responseListener.executeOnExecutor(THREAD_POOL_EXECUTOR, innerInput);
		else
			responseListener.execute(innerInput);
		
		// Begin looping
		idling = false;
		while(!isCancelled()) {
			AnalogueData analogue = new AnalogueData(
					info.callbacks.getAccelerometerValues(),
					info.callbacks.getGyroscopeValues(),
					info.callbacks.getWorldRotation(),
					info.reverseX, info.reverseY);
			try {
				BinarySerialiser.writeBinary(dataOutput, analogue, info.callbacks.getScreenData());
				
				// Reset button press overrides
				info.callbacks.getScreenData().resetOverrides();
				
				safeSleep((long)(info.interval * 1000L));
				
				ClientMessage msg = null;
				if((msg = clientMessages.poll()) != null) {
					switch(msg.what) {
					case ClientMessage.CMD_STOP:
						publishProgress(new Progress(STATE_WAITING, ""));
						closeConnections(socket, protocol, dataOutput, bufferedOutput);
						responseListener.cancel(true);
						return false;
					}
				}
				
			} catch (IOException e) {
				Log.w(TAG, "Lost connection with computer", e);
				closeConnections(socket, protocol, dataOutput, bufferedOutput);
				publishProgress(new Progress(STATE_CONNECTION_LOST, ""));
				responseListener.cancel(true);
				return true; // true means we want another connection
			}
		}
		// If we get to here, user must have cancelled the loop
		Log.i(TAG, "Sending stop signal over connection");
		try {
			BinarySerialiser.writeStopCommand(dataOutput);
			publishProgress(new Progress(STATE_WAITING, ""));
		} catch(IOException e) {
			Log.w(TAG, "Failed to send stop message to server", e);
		}
		closeConnections(socket, protocol, dataOutput, bufferedOutput);
		responseListener.cancel(true);
		
		return false;
	}
	
	class ResponseListener extends AsyncTask<InputStream, Void, Void> {
		@Override
		protected Void doInBackground(InputStream... params) {
			InputStream input = params[0];
			if(input == null) {
				Log.e(TAG, "responseListener InputStream was null");
				return null;
			}
			BufferedInputStream buf = new BufferedInputStream(input);
			DataInputStream data = new DataInputStream(buf);
			byte[] header = new byte[4];
			while(!isCancelled()) {
				try {
					data.read(header, 0, 4);
				} catch (IOException e) {
					Log.w(TAG, "Failed to read header from input stream.", e);
					continue;
				}
				if(header.equals("DCMD".getBytes())) {
					try {
						int command = data.readInt();
						if(clientMessages != null)
							clientMessages.add(new ClientMessage(command));
					} catch (IOException e) {
					Log.w(TAG, "Failed to read command from input stream.", e);
					}
				}
			}
			return null;
		}
	};
	
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
	
	private void closeConnections(Socket socket, TlsProtocolHandler proto, Closeable... closeables) {
		closeConnections(closeables);
		try {
			if(proto != null) proto.close();
			if(socket != null) socket.close();
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
