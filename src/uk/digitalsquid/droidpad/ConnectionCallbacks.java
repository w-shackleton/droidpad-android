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

import uk.digitalsquid.droidpad.buttons.Layout;
import android.app.Application;


public interface ConnectionCallbacks {
	/**
	 * Called when the connection is successfully closed. The connection thread
	 * should now end.
	 */
	void onConnectionFinished();
	
	Layout getScreenData();

	void broadcastState(int status, String connectedPc);
	
	Vec3 getAccelerometerValues();
	Vec3 getGyroscopeValues();
	float getWorldRotation();
	
	public Application getApplication();
}
