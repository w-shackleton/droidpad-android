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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import uk.digitalsquid.droidpad.buttons.Item;
import uk.digitalsquid.droidpad.buttons.Layout;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

public class DroidPadConn implements Runnable {
	private ServerSocket ss;
	private Socket s;
	private OutputStream os;
	private InputStream is;
	private InputStreamReader isr;
	
	private int port = 0;
	private long interval = 50;
	private String mode;
	private DroidPadServer parent;
	private boolean stopping = false;
	
	private int fb = 1;
	private String fbs = "";
	
	private boolean invX = false, invY = false;
	// NORMAL THREAD
	public DroidPadConn(DroidPadServer droidPadServer, int Port, int interval2, String Mode, boolean invX, boolean invY)
	{
		parent = droidPadServer;
		port = Port;
		interval = (long) (float) ((1 / (float) interval2) * 1000);
		
		this.invX = invX;
		this.invY = invY;
		
		mode = Mode;
		//Toast.makeText(parent, String.valueOf(interval), Toast.LENGTH_SHORT).show();
		Log.v("DroidPad", "DPC: Infos recieved (initiated)");
	}
	@Override
	public void run() {
		Log.d("DroidPad", "DPC: Thread started. Waiting for connection...");
		server();
		Log.d("DroidPad", "DPC: Someone has connected!");
		
		
		float[] AVals;
		String str = "";
		Layout buttons;
		while(!stopping)
		{
			if(s != null)
			{
				try
				{
					AVals = parent.getAVals();
					str = "[{" + (invX ? AVals[0] : -AVals[0]) + "," + (invY ? AVals[1] : -AVals[1]) + "," + AVals[2] + "}";
					buttons = parent.getButtons();
					if(buttons != null)
					{
						for(Item item : buttons)
						{
							str += ";";
							str += item.getOutputString();
						}
					}
					str += "]\n"; // [] for easy string view
					
					os.write(str.getBytes());
					os.flush();
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
						if(st.startsWith("<STOP>"))
						{
							//if(ap != null)
							//{
							//	ap.test(5);
							//}
							setConnectedStatus(false,"0.0.0.0");
							server();
						}
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					if(!stopping)
					{
						Log.d("DroidPad", "DPC: Waiting for connection...");
						server();
						Log.d("DroidPad", "DPC: Someone else has connected!");
					}
				}
			}
		}
	}
	//END NORMAL THREAD
	
	//SETUP
	public final synchronized void killThread()
	{
		Log.d("DroidPad", "DPC: Thread dying...");
		if(!stopping)
		{
			stopping = true;
			if(isr != null)
			{
				try {
					isr.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if(is != null)
			{
				try {
					is.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if(os != null)
			{
				try {
					os.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if(s != null)
			{
				try {
					s.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if(ss != null)
			{
				try {
					ss.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Log.d("DroidPad", "DPC: Thread dead!");
	}
	//END SETUP
	
	//MAIN WINDOW NOTIFY
	public final synchronized void notifyOfParent()
	{
		if(parent.isWinAvailable())
		{
			//Toast.makeText(parent, "Parent Disconnected", Toast.LENGTH_SHORT).show();
		}
		else
		{
			//Toast.makeText(parent, "Parent Connected", Toast.LENGTH_SHORT).show();
			if(s != null)
			{
				if(s.isConnected())
				{
					setConnectedStatus(true, s.getInetAddress().getHostAddress());
				}
			}
		}
	}
	private void setConnectedStatus(boolean activated,String ip)
	{
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putString("ip", ip);
		msg.arg1 = activated ? 1 : 0;
		msg.setData(b);
		if(parent.isWinAvailable())
			parent.getWin().isConnectedCallback.sendMessage(msg);
	}
	//END MAIN WINDOW NOTIFY
	
	//SERVER METHODS
	private void server()
	{
		serverInit();
		if(stopping) return; // Escape method
		serverMainLoop();
		if(!stopping)
		{
			try {
				s.setTcpNoDelay(true);
			}
			catch (SocketException e) {
				e.printStackTrace();
			}
			try {
				s.setKeepAlive(true);
			} catch (SocketException e) {
				e.printStackTrace();
			}
			serverSetup();
			setConnectedStatus(true, s.getInetAddress().getHostAddress());
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
					Log.e("DroidPad", "DPC: Couldn't initiate ServerSocket, perhaps not connected to network...");
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
			try
			{
				ss.setSoTimeout(2000);
			} catch (SocketException e)
			{
				e.printStackTrace();
				Log.w("DroidPad", "DPC: Couldn't set timeout");
			}
		}
	}
	private void serverMainLoop()
	{
		while(!stopping)
		{
			try {
				s = ss.accept();
				return;
			} catch (IOException e) {
				fbs = String.valueOf(fb);
				if(fb % 15 == 0)
					fbs = "FIZZBUZZ";
				else
				{
					if(fb % 5 == 0)
						fbs = "BUZZ";
					if(fb % 3 == 0)
						fbs = "FIZZ";
				}
				Log.v("DroidPad", "DPC: Timed out, retrying... (" + fbs + " retries)");
				fb++;
			}
		}
	}
	private void serverSetup()
	{
		try {
			os = s.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("DroidPad", "DPC: Couldn't create output stream");
		}
		try {
			is = s.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("DroidPad", "DPC: Couldn't create input stream");
		}
		isr = new InputStreamReader(is);
		try {
			os.write(("<MODE>" + mode + "</MODE>\n").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("DroidPad", "DPC: Error sending info to PC");
		}
	}
}
