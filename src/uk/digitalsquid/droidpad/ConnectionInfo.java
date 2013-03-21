package uk.digitalsquid.droidpad;

import org.spongycastle.crypto.tls.TlsPSKIdentity;

import uk.digitalsquid.droidpad.buttons.ModeSpec;

public final class ConnectionInfo {
	public int port, securePort;
	/**
	 * The interval between sending updates IN SECONDS
	 */
	public float interval;
	public ConnectionCallbacks callbacks;
	public ModeSpec spec;
	public boolean reverseX, reverseY;
	
	public TlsPSKIdentity identity;
}