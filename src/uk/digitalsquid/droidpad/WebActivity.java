package uk.digitalsquid.droidpad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Shows a local webpage
 * @author william
 *
 */
public class WebActivity extends Activity {
	
	public static final String URL = "uk.digitalsquid.droidpad.WebActivity.URL";
	
	private WebView wv;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web_activity);
		wv = (WebView) findViewById(R.id.webView);
		wv.setWebViewClient(new WebViewClient() {
			@SuppressLint("DefaultLocale")
			@Override
			public boolean shouldOverrideUrlLoading(WebView wv, String url) {
				super.shouldOverrideUrlLoading(wv, url);
				if(url.equals("back:///")) {
					WebActivity.this.finish();
					return true;
				}
				if(url.toLowerCase().startsWith("file:")) {
					Intent intent = new Intent(WebActivity.this, WebActivity.class);
					intent.putExtra(URL, url);
					startActivity(intent);
					return true;
				} else {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(intent);
					return true;
				}
			}
		});
		String url = getIntent().getStringExtra(URL);
		if(url == null) url = "file:///android_asset/intro/start.html";
		wv.loadUrl(url);
	}

}
