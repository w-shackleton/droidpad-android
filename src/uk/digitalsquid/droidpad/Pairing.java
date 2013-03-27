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

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.UUID;

import uk.digitalsquid.ext.Base64;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Pairing implements LogTag {
	
	private final PairedDevices pairingDB;
	
	public Pairing(Context context) {
		pairingDB = new PairedDevices(context);
	}
	
	public static final class DevicePair {
		private final UUID computerId;
		private final String computerName;
		private final UUID deviceId;
		private final byte[] psk;
		
		public DevicePair(UUID computerId, String computerName, UUID deviceId, byte[] psk) {
			this.computerId = computerId;
			this.computerName = computerName;
			this.deviceId = deviceId;
			this.psk = psk;
		}

		public UUID getComputerId() {
			return computerId;
		}

		public String getComputerName() {
			return computerName;
		}

		public UUID getDeviceId() {
			return deviceId;
		}

		public byte[] getPsk() {
			return psk;
		}
	}
	
	protected static class PairedDevices extends SQLiteOpenHelper {
		public static final String DB_NAME = "pairing";
		public static final int DB_VERSION = 1;
		public PairedDevices(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL("CREATE TABLE pairs (" +
						"id INTEGER PRIMARY KEY NOT NULL," +
						"computerId TEXT NOT NULL," +
						"computerName TEXT," +
						"deviceId TEXT NOT NULL," +
						"psk TEXT NOT NULL);");
			} catch(SQLException e) {
				Log.e(TAG, "Failed to create DB tables", e);
			}
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
		
		public void addPairing(UUID computerId, String computerName, UUID deviceId, byte[] psk) throws IOException {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues(4);
			values.put("computerId", computerId.toString());
			values.put("computerName", computerName);
			values.put("deviceId", deviceId.toString());
			values.put("psk", Base64.encodeBytes(psk));
			if(db.insert("pairs", null, values) == -1)
				throw new IOException("Failed to add a new device pair to the database");
			db.close();
		}
		
		public DevicePair findPairing(UUID computerId) throws IOException {
			SQLiteDatabase db = getReadableDatabase();
			Cursor c = db.query("pairs",
					new String[] {
						"computerName",
						"deviceId",
						"psk"},
					"computerId = ?",
					new String[] {
						computerId.toString()
					},
					null, null, null);
			if(c.getCount() < 1) return null;
			c.moveToFirst();
			UUID deviceId;
			String computerName; byte[] psk;
			computerName = c.getString(0);
			deviceId = UUID.fromString(c.getString(1));
			psk = Base64.decode(c.getString(2));
			c.close();
			db.close();
			return new DevicePair(computerId, computerName, deviceId, psk);
		}
	}
	
	/**
	 * Adds a new paired device to the database. String must be in the form:
	 * "Computer uuid
	 * Computer name
	 * Device uuid
	 * PSK (base64)"
	 * @param pairingString The {@link String} to add
	 * @throws IOException An error occurs during database IO
	 * @throws IllegalArgumentException An error occurs decoding the data
	 */
	public DevicePair pairNewDevice(String pairingString) throws IOException, IllegalArgumentException {
		StringTokenizer tkz = new StringTokenizer(pairingString, "\n");
		if(tkz.countTokens() < 4)
			throw new IllegalArgumentException("Incorrect format for pairing string");
		UUID computerId, deviceId;
		String computerName; byte[] psk;
		try {
			computerId = UUID.fromString(tkz.nextToken());
			computerName = tkz.nextToken();
			deviceId = UUID.fromString(tkz.nextToken());
			psk = Base64.decode(tkz.nextToken());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Incorrect format for pairing string", e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Incorrect format for pairing string", e);
		}
		pairingDB.addPairing(computerId, computerName, deviceId, psk);
		return new DevicePair(computerId, computerName, deviceId, psk);
	}
	
	public DevicePair findDevicePair(String computerId) {
		try {
			return findDevicePair(UUID.fromString(computerId));
		} catch(NullPointerException e) {
			Log.e(TAG, "Incorrectly formatted uuid", e);
		} catch(IllegalArgumentException e) {
			Log.e(TAG, "Incorrectly formatted uuid", e);
		}
		return null;
	}
	
	/**
	 * finds a device pair in the DB. Returns <code>null</code> if no pair found.
	 * @param computerId
	 * @return
	 */
	public DevicePair findDevicePair(UUID computerId) {
		try {
			return pairingDB.findPairing(computerId);
		} catch (IOException e) {
			Log.e(TAG, "Couldn't find device pairing", e);
		} catch (NullPointerException e) {
			Log.e(TAG, "Couldn't find device pairing", e);
		}
		return null;
	}
}
