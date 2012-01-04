package com.idunnolol.ragefaces.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.idunnolol.ragefaces.R;
import com.idunnolol.ragefaces.RageFacesApp;

public class ShareUtils {

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
		Log.i(RageFacesApp.TAG, "Rage directory: " + rageDir);
		if (!rageDir.exists()) {
			Log.d(RageFacesApp.TAG, "Rage face directory does not exist, creating it.");
			rageDir.mkdirs();
		}

		File nomediaFile = new File(rageDir, ".nomedia");
		if (!nomediaFile.exists()) {
			Log.d(RageFacesApp.TAG, ".nomedia file does not exist, creating it.");
			try {
				nomediaFile.createNewFile();
			}
			catch (IOException e) {
				Log.e(RageFacesApp.TAG, "Could not create .nomedia file", e);
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
		if (!isMediaMounted()) {
			Toast.makeText(context, context.getString(R.string.err_sd_not_mounted), Toast.LENGTH_LONG).show();
			return null;
		}

		File rageFaceFile = new File(getRageDir(context), name + ".png");
		if (!rageFaceFile.exists()) {
			// File doesn't exist, copy it in
			try {
				InputStream in = context.getResources().openRawResource(resId);
				OutputStream out = new FileOutputStream(rageFaceFile);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}
				out.close();
				in.close();
			}
			catch (IOException e) {
				Log.e(RageFacesApp.TAG, "Could not copy ragefaces file " + name + ".png", e);
				Toast.makeText(context, context.getString(R.string.err_load_face), Toast.LENGTH_LONG).show();
				return null;
			}
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
		File dir = null;
		if (Build.VERSION.SDK_INT >= 8) {
			dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
		intent.setType("image/png");

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
		dummy.setType("image/png");

		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(dummy, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	public static void shareRageFaceSenseMessaging(Context context, Uri rageFaceUri) {
		Intent intent = new Intent("android.intent.action.SEND_MSG");
		intent.putExtra(Intent.EXTRA_STREAM, rageFaceUri);
		intent.setType("image/png");
		context.startActivity(intent);
	}
}
