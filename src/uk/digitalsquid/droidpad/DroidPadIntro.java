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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;

public class DroidPadIntro extends Activity implements OnClickListener {
	
	WebView web;
	Button doneB;
	ImageButton backB, forwardB;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        doneB = (Button)findViewById(R.id.helpDoneButton);
        doneB.setOnClickListener(this);
        backB = (ImageButton)findViewById(R.id.introBackButton);
        backB.setOnClickListener(this);
        forwardB = (ImageButton)findViewById(R.id.introForwardButton);
        forwardB.setOnClickListener(this);
        
        web = (WebView)findViewById(R.id.helpWebview);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setLoadsImagesAutomatically(true);
        //web.loadData(selectPage("dp_main"), "text/html",  "UTF-8");
        web.loadUrl("file://" + getFileStreamPath("").getAbsolutePath() + "/index.html");
        web.setWebViewClient(new wvc());

        backB.setEnabled(false);
        forwardB.setEnabled(false);
    }
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId())
		{
		case R.id.helpDoneButton:
			finish();
			break;
		case R.id.introBackButton:
			web.goBack();
			break;
		case R.id.introForwardButton:
			web.goForward();
		}
	}
	private class wvc extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	if(url.startsWith("http://"))
	    	{
	    		startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));
	    		return true;
	    	}
	    	if(url.startsWith("mailto://"))
	    	{
	    		startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));
	    		return true;
	    	}
	        return false;
	    }
	    @Override
	    public void onPageFinished(WebView view, String url)
	    {
	    	backB.setEnabled(view.canGoBack());
	    	forwardB.setEnabled(view.canGoForward());
	    }
	}
}
