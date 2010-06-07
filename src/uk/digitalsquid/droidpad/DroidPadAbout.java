package uk.digitalsquid.droidpad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DroidPadAbout extends Activity implements OnClickListener{
	// ACTIVITY
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        ((Button)findViewById(R.id.GoIntroButton)).setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		startActivity(new Intent(DroidPadAbout.this,DroidPadIntro.class));
	}
}
