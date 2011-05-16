package com.idunnolol.ragefaces;

import com.idunnolol.ragefaces.data.DatabaseHelper;

import android.app.Application;

public class RageFacesApp extends Application {

	// Logging tag
	public static final String TAG = "rageface";

	@Override
	public void onCreate() {
		super.onCreate();

		DatabaseHelper.createOrUpdateDatabase(this);
	}
}
