package com.idunnolol.ragefaces.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.idunnolol.utils.Log;

public class DatabaseHelper {

	// Increase this number to cause a DB update
	private static final int DB_VERSION = 9;

	private static final String DB_NAME = "faces.db";

	private static final String DB_PREFERENCE_KEY = "db_version";

	public static boolean createOrUpdateDatabase(Context context) {
		boolean createDb = false;

		File dbDir = getDbDir(context);
		File dbFile = getDbPath(context);
		if (!dbDir.exists()) {
			Log.i("Faces database does not exist, creating it.");
			dbDir.mkdir();
			createDb = true;
		}
		else if (!dbFile.exists()) {
			Log.i("Faces database does not exist, creating it.");
			createDb = true;
		}
		else {
			// Check that we have the latest version of the db
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			if (prefs.getInt(DB_PREFERENCE_KEY, 0) < DB_VERSION) {
				Log.i("Faces database is out of date, creating new version.");
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
				Log.e("Could not create faces database.", e);
				return false;
			}

			// Store the current version # of the db
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			Editor editor = prefs.edit();
			editor.putInt(DB_PREFERENCE_KEY, DB_VERSION);
			editor.commit();
		}

		return true;
	}

	public static SQLiteDatabase getFacesDb(Context context) {
		File dbPath = getDbPath(context);
		return SQLiteDatabase.openDatabase(dbPath.getPath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	}

	private static File getDbDir(Context context) {
		return new File("/data/data/" + context.getPackageName() + "/databases/");
	}

	private static File getDbPath(Context context) {
		return new File(getDbDir(context), DB_NAME);
	}

	private static final String QUERY_BEGINNING = "SELECT F._id, F.drawable FROM (SELECT FC.faceId AS faceId, min(C.position) AS pos FROM FaceCategories FC, Categories C WHERE FC.categoryId == C._id ";
	private static final String QUERY_END = " GROUP BY FC.faceId ORDER BY pos ASC) T, Faces F WHERE F._id == T.faceId ORDER BY T.pos ASC, F.drawable";

	public static Cursor getFaces(SQLiteDatabase db, List<Integer> categories) {
		if (!db.isOpen()) {
			return null;
		}

		if (categories == null || categories.size() == 0) {
			return db.rawQuery(QUERY_BEGINNING + QUERY_END, null);
		}
		else {
			int len = categories.size();
			String[] selection = new String[len];
			StringBuilder sb = new StringBuilder();
			for (int a = 0; a < len; a++) {
				selection[a] = categories.get(a) + "";
				if (a > 0) {
					sb.append(", ");
				}
				sb.append("?");
			}
			return db.rawQuery(QUERY_BEGINNING + " AND C._id IN (" + sb.toString() + ")" + QUERY_END, selection);
		}
	}

	public static Cursor getCategories(SQLiteDatabase db) {
		if (!db.isOpen()) {
			return null;
		}

		return db.query("Categories", new String[] { "_id", "category" }, null, null, null, null, "category");
	}
}
