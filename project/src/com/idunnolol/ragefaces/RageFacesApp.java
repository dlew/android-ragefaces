package com.idunnolol.ragefaces;

import com.idunnolol.utils.Log;

import android.app.Application;

public class RageFacesApp extends Application {

	// Logging tag
	public static final String TAG = "RageFaces";

	@Override
	public void onCreate() {
		super.onCreate();

		Log.configure(TAG, true);
	}
}
