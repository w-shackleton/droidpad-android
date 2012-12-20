package uk.digitalsquid.droidpad2;

import uk.digitalsquid.droidpad2.buttons.Layout;

public interface ConnectionCallbacks {
	/**
	 * Called when the connection is successfully closed. The connection thread
	 * should now end.
	 */
	void onConnectionFinished();
	
	Layout getScreenData();
}
