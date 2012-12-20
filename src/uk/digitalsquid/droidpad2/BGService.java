package uk.digitalsquid.droidpad2;

import uk.digitalsquid.droidpad2.buttons.Layout;
import uk.digitalsquid.droidpad2.buttons.ModeSpec;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

/**
 * Backend service that runs the service that communicates with the computer.
 * @author william
 *
 */
public class BGService extends Service implements ConnectionCallbacks, LogTag {
	
	public static final String MODE_SPEC = "uk.digitalsquid.droidpad2.BGService.ModeSpec";
	
	public static final String INTENT_STATUSUPDATE = "uk.digitalsquid.droidpad2.BGService.Status";
	public static final String INTENT_EXTRA_STATE = "uk.digitalsquid.droidpad2.BGService.Status.State";
	public static final String INTENT_EXTRA_IP = "uk.digitalsquid.droidpad2.BGService.Status.Ip";
	public static final int STATE_CONNECTED = 1;
	public static final int STATE_WAITING = 2;
	public static final int STATE_CONNECTION_LOST = 3;
	
	public static final class Calibration {
		public final float x, y;
		public Calibration(float x, float y) {
			this.x = x;
			this.y = y;
		}
		public Calibration(SharedPreferences prefs) {
			// FIXME: Use proper calibration rather than this?
			x = prefs.getFloat("calibX", 0);
			y = prefs.getFloat("calibY", 0);
		}
		
		public void save(SharedPreferences prefs) {
			Editor prefEd = prefs.edit();
			prefEd.putFloat("calibX", x);
			prefEd.putFloat("calibY", y);
			prefEd.commit();
		}
	}
	
	private SharedPreferences prefs;
	
	private int state = 0;
	private String connectedPc = "Computer"; // Temp placeholder text
	
	private ModeSpec spec;
	private Calibration calibration;
	
	private Connection connection;
	
	private Vec3 accelerometer;
	/**
	 * Data which the user has inputed onscreen.
	 */
	private Layout screenData;
	
	@Override
	public void onCreate() {
		super.onCreate();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		calibration = new Calibration(prefs);
		// Load basic preferences (MINIMAL)
		// Acquire wifi and multicast locks
		// Start mDNS broadcaster
	}
	
	/**
	 * Call this method when the user has chosen a mode to use. The mode that
	 * then should be displayed will then be returned; most of the time the
	 * mode the user chose will be returned, but in some cases the service
	 * will override the user's choice (for example if a session is running
	 * already).
	 * @param spec
	 * @return
	 */
	public synchronized ModeSpec onModeChosen(ModeSpec spec) {
		if(connection != null) {
			if(connection.attemptClose()) { // Closed idle connection successfully
				createNewConnection(spec);
				this.spec = spec;
				return spec;
			} else { // Connection running, user can't change mode.
				return this.spec;
			}
		} else { // No existing connection
			createNewConnection(spec);
			this.spec = spec;
			return null;
		}
	}
	
	private synchronized void createNewConnection(ModeSpec newSpec) {
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		// Force stop connection if running
		// Stop mDNS broadcaster
		// Release wifi and multicast locks
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
    public class LocalBinder extends Binder {
        BGService getService() {
            return BGService.this;
        }
    }
    private final IBinder binder = new LocalBinder();
    
    long gyroIntegrationTime;
    boolean gyroAvailable = false;
    
    private SensorEventListener sensorEvents = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
		}
    };
    
    void broadcastState() {
    	Intent intent = new Intent(INTENT_STATUSUPDATE);
    	intent.putExtra(INTENT_EXTRA_STATE, state);
    	intent.putExtra(INTENT_EXTRA_IP, connectedPc);
    	sendBroadcast(intent);
    }
    
    /**
     * Sends out a broadcast intent with information about whether DroidPad is connected.
     * @param state
     * @param connectedPc
     */
    void broadcastState(int state, String connectedPc) {
    	this.state = state;
    	this.connectedPc = connectedPc;
    	broadcastState();
    }
    
    public void calibrate() {
    	if(accelerometer != null) {
    		calibration = new Calibration(accelerometer.x, accelerometer.y);
    	}
    }

    @Override
	public Layout getScreenData() {
		return screenData;
	}

	public void setScreenData(Layout screenData) {
		this.screenData = screenData;
	}

	@Override
	public void onConnectionFinished() {
		// TODO Auto-generated method stub
		
	}
}
