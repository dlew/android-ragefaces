package com.idunnolol.ragefaces.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.idunnolol.ragefaces.RageFacesApp;

public class DatabaseHelper {

	// Increase this number to cause a DB update
	private static final int DB_VERSION = 1;

	private static final String DB_NAME = "faces.db";

	private static final String DB_PREFERENCE_KEY = "db_version";

	public static boolean createOrUpdateDatabase(Context context) {
		boolean createDb = false;

		File dbDir = getDbDir(context);
		File dbFile = getDbPath(context);
		if (!dbDir.exists()) {
			Log.i(RageFacesApp.TAG, "Faces database does not exist, creating it.");
			dbDir.mkdir();
			createDb = true;
		}
		else if (!dbFile.exists()) {
			Log.i(RageFacesApp.TAG, "Faces database does not exist, creating it.");
			createDb = true;
		}
		else {
			// Check that we have the latest version of the db
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			if (prefs.getInt(DB_PREFERENCE_KEY, 0) < DB_VERSION) {
				Log.i(RageFacesApp.TAG, "Faces database is out of date, creating new version.");
				dbFile.delete();
				createDb = true;
			}
		}

		if (createDb) {
			try {
				// Open your local db as the input stream
				InputStream myInput = context.getAssets().open(DB_NAME);

				// Open the empty db as the output stream
				OutputStream myOutput = new FileOutputStream(dbFile);

				// transfer bytes from the inputfile to the outputfile
				byte[] buffer = new byte[1024];
				int length;
				while ((length = myInput.read(buffer)) > 0) {
					myOutput.write(buffer, 0, length);
				}

				// Close the streams
				myOutput.flush();
				myOutput.close();
				myInput.close();
			}
			catch (IOException e) {
				Log.e(RageFacesApp.TAG, "Could not create faces database.", e);
				return false;
			}

			// Store the current version # of the db
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			Editor editor = prefs.edit();
			editor.putInt(DB_PREFERENCE_KEY, DB_VERSION);
		}

		return true;
	}

	public static SQLiteDatabase getFacesDb(Context context) {
		File dbPath = getDbPath(context);
		return SQLiteDatabase.openDatabase(dbPath.getPath(), null, SQLiteDatabase.OPEN_READONLY);
	}

	private static File getDbDir(Context context) {
		return new File("/data/data/" + context.getPackageName() + "/databases/");
	}

	private static File getDbPath(Context context) {
		return new File(getDbDir(context), DB_NAME);
	}
}
