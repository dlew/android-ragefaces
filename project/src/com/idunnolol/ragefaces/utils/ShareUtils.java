package com.idunnolol.ragefaces.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.widget.Toast;

import com.idunnolol.ragefaces.R;
import com.idunnolol.utils.Log;

public class ShareUtils {

	private static boolean sUseBackupRageDir = false;

	private static final String README_FILE = "README.TXT";

	/**
	 * Loads the media drive directory where we place the rage faces for sharing.
	 * 
	 * @return 0 on success; otherwise returns the resId of an error message.
	 */
	public static int loadRageFacesDir(Context context) {
		if (!isMediaMounted()) {
			return R.string.err_sd_not_mounted;
		}

		File rageDir = getRageDir(context);
		Log.i("Rage directory: " + rageDir);
		if (!rageDir.exists()) {
			Log.d("Rage face directory does not exist, creating it.");
			boolean success = rageDir.mkdirs();
			if (!success && !sUseBackupRageDir) {
				sUseBackupRageDir = true;
				return loadRageFacesDir(context);
			}
		}

		File nomediaFile = new File(rageDir, ".nomedia");
		if (!nomediaFile.exists()) {
			Log.d(".nomedia file does not exist, creating it.");
			try {
				nomediaFile.createNewFile();
			}
			catch (IOException e) {
				Log.e("Could not create .nomedia file", e);
				return R.string.err_loading;
			}
		}

		File readmeFile = new File(rageDir, README_FILE);
		if (!readmeFile.exists()) {
			Log.d("readme does not exist, creating it.");

			try {
				readmeFile.createNewFile();

				// Write a short readme
				FileOutputStream fOut = new FileOutputStream(readmeFile);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fOut));
				writer.write(context.getString(R.string.readme_text));
				writer.close();
			}
			catch (IOException e) {
				Log.e("Could not create readme file", e);
				return R.string.err_loading;
			}
		}

		return 0;
	}

	/**
	 * Loads a rage face to the SD card if it is not already there.
	 * 
	 * @param context the context
	 * @param name the name of the rage face
	 * @param resId the raw resource id for the rage face
	 * @return the URI leading to the loaded rage face, or null if it could not be loaded 
	 */
	public static Uri loadRageFace(Context context, String name, int resId) {
		Log.i("Loading rage face \"" + name + "\"");

		if (!isMediaMounted()) {
			Toast.makeText(context, context.getString(R.string.err_sd_not_mounted), Toast.LENGTH_LONG).show();
			return null;
		}

		boolean useJpeg = useJpeg(context);

		String type = (useJpeg) ? ".jpg" : ".png";
		String filename = name + type;
		File rageFaceFile = new File(getRageDir(context), filename);
		Log.d("Face filename: " + rageFaceFile.toString());
		if (rageFaceFile.exists()) {
			Log.v("Previous version of \"" + filename + "\" exists, deleting.");
			rageFaceFile.delete();
		}

		// File doesn't exist, copy it in
		try {
			OutputStream out = new FileOutputStream(rageFaceFile);

			if (useJpeg) {
				Bitmap source = BitmapFactory.decodeStream(context.getResources().openRawResource(resId));
				source.compress(CompressFormat.JPEG, 75, out);
			}
			else {
				InputStream in = context.getResources().openRawResource(resId);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}
				in.close();
			}

			out.close();
		}
		catch (IOException e) {
			Log.e("Could not copy ragefaces file " + rageFaceFile.getAbsolutePath(), e);
			Toast.makeText(context, context.getString(R.string.err_load_face), Toast.LENGTH_LONG).show();
			return null;
		}

		return Uri.fromFile(rageFaceFile);
	}

	/**
	 * Adds a rage face to the phone's gallery
	 * 
	 * @param context the context
	 * @param name the name of the rage face
	 * @param resId the raw resource id for the rage face
	 */
	public static String addRageFaceToGallery(Context context, String name, int resId) {
		Bitmap source = BitmapFactory.decodeStream(context.getResources().openRawResource(resId));
		return MediaStore.Images.Media.insertImage(context.getContentResolver(), source, name, null);
	}

	private static boolean isMediaMounted() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	// Uses rules outlined here: http://developer.android.com/guide/topics/data/data-storage.html#AccessingExtFiles
	private static File getRageDir(Context context) {
		if (sUseBackupRageDir) {
			return new File(Environment.getExternalStorageDirectory(), "com.idunnolol.rageface/");
		}

		File dir = null;
		if (sExternalFilesDirAvailable) {
			dir = DirWrapper.getExternalFilesDir(context, DirWrapper.getPicturesDirectoryType());
		}

		if (dir == null) {
			dir = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName()
					+ "/files/Pictures/");
		}

		return dir;
	}

	public static void shareRageFace(Context context, Uri rageFaceUri) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_STREAM, rageFaceUri);
		intent.setType(getShareType(context));

		Intent chooser = Intent.createChooser(intent, null);
		context.startActivity(chooser);
	}

	/**
	 * This tests to see if there is a SEND_MSG intent.  If there is,
	 * it means we're on an asshole HTC Sense machine, and have to give
	 * the user an explicit option to send via Messaging or some other app.
	 * @return
	 */
	public static boolean hasSenseMessagingApp(Context context, Uri rageFaceUri) {
		Intent dummy = new Intent("android.intent.action.SEND_MSG");
		dummy.putExtra(Intent.EXTRA_STREAM, rageFaceUri);
		dummy.setType(getShareType(context));

		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(dummy, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	public static void shareRageFaceSenseMessaging(Context context, Uri rageFaceUri) {
		Intent intent = new Intent("android.intent.action.SEND_MSG");
		intent.putExtra(Intent.EXTRA_STREAM, rageFaceUri);
		intent.setType(getShareType(context));
		context.startActivity(intent);
	}

	public static String getShareType(Context context) {
		return (useJpeg(context)) ? "image/jpeg" : "image/png";
	}

	public static boolean useJpeg(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String outputPref = (prefs.getString(context.getString(R.string.key_format),
				context.getString(R.string.key_format_default)));
		return outputPref.equals(context.getString(R.string.key_format_jpeg));
	}

	//////////////////////////////////////////////////////////////////////////

	private static boolean sExternalFilesDirAvailable;

	static {
		try {
			DirWrapper.checkAvailable();
			sExternalFilesDirAvailable = true;
		}
		catch (Throwable t) {
			sExternalFilesDirAvailable = false;
		}
	}

	private static class DirWrapper {
		static {
			try {
				Context.class.getMethod("getExternalFilesDir", String.class);
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		public static void checkAvailable() { }

		@TargetApi(8)
		public static String getPicturesDirectoryType() {
			return Environment.DIRECTORY_PICTURES;
		}

		@TargetApi(8)
		public static File getExternalFilesDir(Context context, String type) {
			return context.getExternalFilesDir(type);
		}
	}
}
