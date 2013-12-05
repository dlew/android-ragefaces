package com.idunnolol.ragefaces;

import com.crashlytics.android.Crashlytics;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * This ill-named Activity is kept just to be a launcher for the app.
 * 
 * It remains due to the moving of Activities, but may also someday
 * be useful as a routing launcher.
 */
public class RageFacePickerActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Crashlytics.start(this);
		startActivity(new Intent(this, com.idunnolol.ragefaces.app.RageFacePickerActivity.class));
		finish();
	}
}