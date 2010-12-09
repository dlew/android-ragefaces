package com.idunnolol.ragefaces;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RageFacePickerActivity extends Activity {

	private static final String TAG = "rageface";
	private static final String RAGE_DIR = "com.idunnolol.rageface/";
	private static final int DIALOG_MSG_SHARE = 1;
	private static final int DIALOG_ABOUT = 2;
	private static final String STATE_RAGEFACE_URI = "STATE_RAGEFACE_URI";

	private Context mContext;
	private Resources mResources;

	private Map<String, Integer> mRageFaces;

	private RageFaceAdapter mAdapter;

	// Cached views 
	private LinearLayout mLoadingContainer;
	private TextView mMessageView;
	private GridView mGridView;

	// When dialog is opened, rage face uri stored here
	private Uri mRageFaceUri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		mResources = getResources();

		setContentView(R.layout.main);
		mLoadingContainer = (LinearLayout) findViewById(R.id.LoadingContainer);
		mMessageView = (TextView) findViewById(R.id.Message);
		mGridView = (GridView) findViewById(R.id.GridView);

		// Use reflection to make a list of all rage faces contained in /res/raw/
		mRageFaces = new HashMap<String, Integer>();
		for (Field field : R.raw.class.getFields()) {
			try {
				mRageFaces.put(field.getName(), field.getInt(null));
			}
			catch (Exception e) {
				Log.e(TAG, "HOW DID THIS HAPPEN FFFFUUUUU", e);
			}
		}

		// Create an adapter
		mAdapter = new RageFaceAdapter(this, mRageFaces);

		// Apply adapter to gridview
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Uri rageFaceUri = loadRageFace((String) mAdapter.getItem(position));
				if (rageFaceUri != null) {
					sendRageFace(rageFaceUri);
				}
			}
		});

		// Load rage faces to SD card
		boolean success = loadRageFacesDir();

		// Supposing everything went fine, load the gridview
		if (success) {
			mLoadingContainer.setVisibility(View.GONE);
			mGridView.setVisibility(View.VISIBLE);
		}

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_RAGEFACE_URI)) {
			mRageFaceUri = savedInstanceState.getParcelable(STATE_RAGEFACE_URI);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mRageFaceUri != null) {
			outState.putParcelable(STATE_RAGEFACE_URI, mRageFaceUri);
		}
	}

	public boolean loadRageFacesDir() {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			mMessageView.setText(R.string.err_sd_not_mounted);
			return false;
		}

		File rageDir = getRageDir();
		if (!rageDir.exists()) {
			Log.d(TAG, "Rage face directory does not exist, creating it.");
			rageDir.mkdir();
		}

		File nomediaFile = new File(rageDir, ".nomedia");
		if (!nomediaFile.exists()) {
			Log.d(TAG, ".nomedia file does not exist, creating it.");
			try {
				nomediaFile.createNewFile();
			}
			catch (IOException e) {
				Log.e(TAG, "Could not create .nomedia file", e);
				mMessageView.setText(R.string.err_loading);
				return false;
			}
		}

		return true;
	}

	// Loads a rage face to the SD card, if it is not already there
	// Returns the URI for it
	private Uri loadRageFace(String name) {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(mContext, getString(R.string.err_sd_not_mounted), Toast.LENGTH_LONG).show();
			return null;
		}

		File rageDir = getRageDir();
		File rageFaceFile = new File(rageDir, name + ".png");
		if (!rageFaceFile.exists()) {
			// File doesn't exist, copy it in
			try {
				InputStream in = mResources.openRawResource(mRageFaces.get(name));
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
				Log.e(TAG, "Could not copy ragefaces file " + name + ".png", e);
				Toast.makeText(mContext, getString(R.string.err_load_face), Toast.LENGTH_LONG).show();
				return null;
			}
		}

		return Uri.fromFile(rageFaceFile);
	}

	private File getRageDir() {
		return new File(Environment.getExternalStorageDirectory(), RAGE_DIR);
	}

	private void sendRageFace(Uri rageFaceUri) {
		// First, test to see if there is a SEND_MSG intent.  If there is
		// it means we're on an asshole HTC Sense machine, and have to give
		// the user an explicit option to send via Messaging or some other app.

		Intent dummy = new Intent("android.intent.action.SEND_MSG");
		dummy.putExtra(Intent.EXTRA_STREAM, rageFaceUri);
		dummy.setType("image/png");

		PackageManager packageManager = getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(dummy, PackageManager.MATCH_DEFAULT_ONLY);
		if (list.size() > 0) {
			mRageFaceUri = rageFaceUri;
			showDialog(DIALOG_MSG_SHARE);
		}
		else {
			shareRageFace(rageFaceUri);
		}
	}

	private void shareRageFaceSenseMessaging(Uri rageFaceUri) {
		Intent intent = new Intent("android.intent.action.SEND_MSG");
		intent.putExtra(Intent.EXTRA_STREAM, rageFaceUri);
		intent.setType("image/png");
		startActivity(intent);
	}

	private void shareRageFace(Uri rageFaceUri) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_STREAM, rageFaceUri);
		intent.setType("image/png");

		Intent chooser = Intent.createChooser(intent, null);
		startActivity(chooser);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.picker_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.About: {
			showDialog(DIALOG_ABOUT);
			return true;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_MSG_SHARE: {
			Builder builder = new Builder(this);
			builder.setTitle(R.string.dialog_chooser_title);
			builder.setItems(R.array.chooser_options, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0) {
						shareRageFaceSenseMessaging(mRageFaceUri);
					}
					else {
						shareRageFace(mRageFaceUri);
					}
				}
			});
			builder.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_MSG_SHARE);
				}
			});
			return builder.create();
		}
		case DIALOG_ABOUT:
			// Try to get the version name
			String versionName;
			try {
				PackageManager pm = getPackageManager();
				PackageInfo pi = pm.getPackageInfo("com.idunnolol.ragefaces", 0);
				versionName = pi.versionName;
			}
			catch (Exception e) {
				// PackageManager is traditionally wonky, need to accept all exceptions here.
				Log.w(TAG, "Couldn't get package info in order to show version #!", e);
				versionName = "";
			}

			Builder builder = new Builder(this);
			builder.setMessage(getString(R.string.about_msg, versionName));
			builder.setNeutralButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_ABOUT);
				}
			});
			builder.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_ABOUT);
				}
			});
			return builder.create();
		}

		return super.onCreateDialog(id);
	}
}