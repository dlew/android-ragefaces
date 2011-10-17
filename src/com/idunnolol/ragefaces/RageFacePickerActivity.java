package com.idunnolol.ragefaces;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.idunnolol.ragefaces.adapters.RageFaceDbAdapter;
import com.idunnolol.ragefaces.adapters.RageFaceScannerAdapter;
import com.idunnolol.ragefaces.adapters.RawRetriever;
import com.idunnolol.ragefaces.data.DatabaseHelper;
import com.idunnolol.ragefaces.data.RageFaceMediaScanner;
import com.idunnolol.ragefaces.utils.ResourceUtils;

public class RageFacePickerActivity extends Activity {

	// Where files go for sharing with other apps
	private static final String RAGE_DIR = "com.idunnolol.rageface/";
	private static final String PUBLIC_RAGE_DIR = "Rage Faces/";

	// For keeping state between configuration changes
	private static final String STATE_RAGEFACE_ID = "STATE_RAGEFACE_ID";
	private static final String STATE_RAGEFACE_NAME = "STATE_RAGEFACE_NAME";
	private static final String STATE_RAGEFACE_URI = "STATE_RAGEFACE_URI";
	private static final String STATE_FILTER_CATEGORY = "STATE_FILTER_CATEGORY";

	// Dialog codes
	private static final int DIALOG_ACTIONS = 1;
	private static final int DIALOG_ABOUT = 2;
	private static final int DIALOG_FILTER = 3;
	private static final int DIALOG_HELP = 4;

	// Cached Activity info
	private Context mContext;
	private Resources mResources;

	private BaseAdapter mAdapter;

	// Cached views 
	private LinearLayout mLoadingContainer;
	private TextView mMessageView;
	private GridView mGridView;

	// When dialog is opened, rage face data stored here
	private int mRageFaceId;
	private String mRageFaceName;
	private Uri mRageFaceUri;

	// Filter data
	private int[] mCategoryIds;
	private String[] mCategoryNames;
	private int mFilterCategory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean dbExists = DatabaseHelper.createOrUpdateDatabase(this);

		mContext = this;
		mResources = getResources();

		setContentView(R.layout.main);
		mLoadingContainer = (LinearLayout) findViewById(R.id.LoadingContainer);
		mMessageView = (TextView) findViewById(R.id.Message);
		mGridView = (GridView) findViewById(R.id.GridView);

		if (savedInstanceState != null) {
			mFilterCategory = savedInstanceState.getInt(STATE_FILTER_CATEGORY, -1);

			if (savedInstanceState.containsKey(STATE_RAGEFACE_URI)) {
				mRageFaceId = savedInstanceState.getInt(STATE_RAGEFACE_ID);
				mRageFaceName = savedInstanceState.getString(STATE_RAGEFACE_NAME);
				mRageFaceUri = savedInstanceState.getParcelable(STATE_RAGEFACE_URI);
			}
		}
		else {
			mFilterCategory = -1;
		}

		// Create an adapter
		if (dbExists) {
			mAdapter = new RageFaceDbAdapter(this);
			filter(mFilterCategory);
		}
		else {
			mAdapter = new RageFaceScannerAdapter(this);
			findViewById(R.id.button_bar_layout).setVisibility(View.GONE);
		}

		// Apply adapter to gridview
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Gather some data on what the user selected
				RawRetriever retriever = (RawRetriever) mAdapter;
				mRageFaceId = retriever.getRawResourceId((String) mAdapter.getItem(position));
				mRageFaceName = (String) mAdapter.getItem(position);
				mRageFaceUri = loadRageFace(mRageFaceName, false);

				Intent intent = getIntent();
				if (intent.getAction().equals(Intent.ACTION_GET_CONTENT)||intent.getAction().equals(Intent.ACTION_PICK)) {
					Intent data = new Intent();
					data.setData(mRageFaceUri);
					setResult(RESULT_OK, data);
					finish();
				}
				else {
					// Default - show dialog with action options to user
					showDialog(DIALOG_ACTIONS);
				}
			}
		});

		// Configure the filter button
		Button filterButton = (Button) findViewById(R.id.filter_button);
		filterButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_FILTER);
			}
		});

		// Configure the filter dialog backing data
		if (dbExists) {
			SQLiteDatabase db = DatabaseHelper.getFacesDb(this);
			Cursor c = DatabaseHelper.getCategories(db);
			c.moveToFirst();
			int len = c.getCount();
			mCategoryIds = new int[len];
			mCategoryNames = new String[len];
			int a = 0;
			while (!c.isAfterLast()) {
				mCategoryIds[a] = c.getInt(0);
				String category = c.getString(1);
				int resId = ResourceUtils.getResourceId(R.string.class, c.getString(1));
				if (resId != -1) {
					category = getString(resId);
				}
				mCategoryNames[a] = category;
				c.moveToNext();
				a++;
			}
			c.close();
			db.close();
		}

		// Load rage faces to SD card
		boolean success = loadRageFacesDir();

		// Supposing everything went fine, load the gridview
		if (success) {
			mLoadingContainer.setVisibility(View.GONE);
			mGridView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(STATE_FILTER_CATEGORY, mFilterCategory);

		if (mRageFaceUri != null) {
			outState.putInt(STATE_RAGEFACE_ID, mRageFaceId);
			outState.putString(STATE_RAGEFACE_NAME, mRageFaceName);
			outState.putParcelable(STATE_RAGEFACE_URI, mRageFaceUri);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Properly close cursor if we're using db-based faces
		if (mAdapter instanceof RageFaceDbAdapter) {
			((RageFaceDbAdapter) mAdapter).shutdown();
		}
	}

	public boolean loadRageFacesDir() {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			mMessageView.setText(R.string.err_sd_not_mounted);
			return false;
		}

		File rageDir = getRageDir();
		if (!rageDir.exists()) {
			Log.d(RageFacesApp.TAG, "Rage face directory does not exist, creating it.");
			rageDir.mkdir();
		}

		File nomediaFile = new File(rageDir, ".nomedia");
		if (!nomediaFile.exists()) {
			Log.d(RageFacesApp.TAG, ".nomedia file does not exist, creating it.");
			try {
				nomediaFile.createNewFile();
			}
			catch (IOException e) {
				Log.e(RageFacesApp.TAG, "Could not create .nomedia file", e);
				mMessageView.setText(R.string.err_loading);
				return false;
			}
		}

		File picturesDir = new File(Environment.getExternalStorageDirectory(), "Pictures/");
		if (!picturesDir.exists()) {
			Log.d(RageFacesApp.TAG, "Pictures media directory does not exist, creating it.");
			picturesDir.mkdir();
		}

		File publicRageDir = getPublicRageDir();
		if (!publicRageDir.exists()) {
			Log.d(RageFacesApp.TAG, "Public rage face directory does not exist, creating it.");
			publicRageDir.mkdir();
		}

		return true;
	}

	// Loads a rage face to the SD card, if it is not already there
	// Returns the URI for it
	private Uri loadRageFace(String name, boolean isPublic) {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(mContext, getString(R.string.err_sd_not_mounted), Toast.LENGTH_LONG).show();
			return null;
		}

		// Just in case, can't hurt.
		loadRageFacesDir();

		File rageDir = (isPublic) ? getPublicRageDir() : getRageDir();
		File rageFaceFile = new File(rageDir, name + ".png");
		if (!rageFaceFile.exists()) {
			// File doesn't exist, copy it in
			try {
				RawRetriever retriever = (RawRetriever) mAdapter;
				InputStream in = mResources.openRawResource(retriever.getRawResourceId(name));
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
				Toast.makeText(mContext, getString(R.string.err_load_face), Toast.LENGTH_LONG).show();
				return null;
			}
		}

		// If this was loaded to be public, do a media scan now to load it into the gallery
		if (isPublic) {
			RageFaceMediaScanner mediaScanner = new RageFaceMediaScanner(this, rageFaceFile.getAbsolutePath());
			mediaScanner.doScan();
		}

		return Uri.fromFile(rageFaceFile);
	}

	private File getRageDir() {
		return new File(Environment.getExternalStorageDirectory(), RAGE_DIR);
	}

	private File getPublicRageDir() {
		return new File(Environment.getExternalStorageDirectory(), "Pictures/" + PUBLIC_RAGE_DIR);
	}

	/**
	 * This tests to see if there is a SEND_MSG intent.  If there is,
	 * it means we're on an asshole HTC Sense machine, and have to give
	 * the user an explicit option to send via Messaging or some other app.
	 * @return
	 */
	private boolean hasSenseMessagingApp(Uri rageFaceUri) {
		Intent dummy = new Intent("android.intent.action.SEND_MSG");
		dummy.putExtra(Intent.EXTRA_STREAM, rageFaceUri);
		dummy.setType("image/png");

		PackageManager packageManager = getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(dummy, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
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
		case R.id.help:
			showDialog(DIALOG_HELP);
			return true;
		case R.id.About:
			showDialog(DIALOG_ABOUT);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ACTIONS: {
			// First, determine which items we will show to the user
			ArrayList<String> items = new ArrayList<String>();
			final ArrayList<DialogAction> actions = new ArrayList<DialogAction>();

			// Standard share option
			items.add(getString(R.string.dialog_actions_opt_share));
			actions.add(new DialogAction() {
				public void doAction() {
					shareRageFace(mRageFaceUri);
				}
			});

			// If the user has Sense messaging, that option
			if (hasSenseMessagingApp(mRageFaceUri)) {
				items.add(getString(R.string.dialog_actions_opt_share_sense));
				actions.add(new DialogAction() {
					public void doAction() {
						shareRageFaceSenseMessaging(mRageFaceUri);
					}
				});
			}

			// View the face full screen
			items.add(getString(R.string.dialog_actions_opt_view));
			actions.add(new DialogAction() {
				public void doAction() {
					Intent intent = new Intent(mContext, RageFaceViewerActivity.class);
					intent.putExtra(RageFaceViewerActivity.EXTRA_FACE_ID, mRageFaceId);
					startActivity(intent);
				}
			});

			// Add the face to the gallery
			items.add(getString(R.string.dialog_actions_opt_add_to_gallery));
			actions.add(new DialogAction() {
				public void doAction() {
					loadRageFace(mRageFaceName, true);
				}
			});

			Builder builder = new Builder(this);
			builder.setTitle(R.string.dialog_actions_title);
			builder.setItems(items.toArray(new String[0]), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					actions.get(which).doAction();
					removeDialog(DIALOG_ACTIONS);
				}
			});
			builder.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_ACTIONS);
				}
			});
			return builder.create();
		}
		case DIALOG_FILTER: {
			Builder builder = new Builder(this);
			builder.setTitle(R.string.dialog_filter_title);
			builder.setItems(mCategoryNames, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					filter(mCategoryIds[which]);
					dismissDialog(DIALOG_FILTER);
				}
			});
			builder.setNeutralButton(R.string.dialog_filter_clear, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					clearFilter();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, null);
			return builder.create();
		}
		case DIALOG_HELP: {
			Builder builder = new Builder(this);
			builder.setMessage(getString(R.string.help_msg));
			builder.setNeutralButton(android.R.string.ok, null);
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
				Log.w(RageFacesApp.TAG, "Couldn't get package info in order to show version #!", e);
				versionName = "";
			}

			Builder builder = new Builder(this);
			builder.setMessage(getString(R.string.about_msg, versionName));
			builder.setNeutralButton(android.R.string.ok, null);
			return builder.create();
		}

		return super.onCreateDialog(id);
	}

	// This is for making it easier to associate each dialog option with an action
	private interface DialogAction {
		public void doAction();
	}

	//////////////////////////////////////////////////////////////////////////
	// Filter stuff

	public void clearFilter() {
		filter(-1);
	}

	public void filter(int category) {
		List<Integer> filteredCategories = null;
		if (category != -1) {
			filteredCategories = new ArrayList<Integer>();
			filteredCategories.add(category);
		}
		RageFaceDbAdapter adapter = (RageFaceDbAdapter) mAdapter;
		adapter.filter(filteredCategories);

		// Setup history for what the last filter was
		mFilterCategory = category;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// If there is a filter active and the user clicks the "back" button, clear the filter
		// instead of backing out of the activity.
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && mFilterCategory != -1) {
			clearFilter();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}