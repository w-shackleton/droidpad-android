package uk.digitalsquid.droidpad2;

import uk.digitalsquid.droidpad2.Connection.ConnectionInfo;
import uk.digitalsquid.droidpad2.buttons.ModeSpec;
import android.os.AsyncTask;

public class Connection extends AsyncTask<ConnectionInfo, Integer, Void> {
	
	public static final class ConnectionInfo {
		public int port;
		/**
		 * The interval between sending updates IN SECONDS
		 */
		public float interval;
		public ConnectionCallbacks callbacks;
		public ModeSpec spec;
	}
	
	private ConnectionInfo info;
	private boolean idling;
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
	}

	@Override
	protected Void doInBackground(ConnectionInfo... infos) {
		info = infos[0];
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		info.callbacks.onConnectionFinished();
	}
	
	/**
	 * Closes the current connection if the thread is idling.
	 * @return <code>true</code> if the attempt succeeded, <code>false</code> otherwise.
	 */
	public boolean attemptClose() {
		// TODO: IMPLEMENT
		if(idling) {
			// CLOSE
		} else {
			// DON'T
		}
		return idling;
	}
}
