package com.idunnolol.ragefaces.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
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

import com.idunnolol.ragefaces.R;
import com.idunnolol.ragefaces.RageFacesApp;
import com.idunnolol.ragefaces.adapters.RageFaceDbAdapter;
import com.idunnolol.ragefaces.adapters.RageFaceScannerAdapter;
import com.idunnolol.ragefaces.adapters.RawRetriever;
import com.idunnolol.ragefaces.data.DatabaseHelper;
import com.idunnolol.ragefaces.data.Pair;
import com.idunnolol.ragefaces.utils.ResourceUtils;
import com.idunnolol.ragefaces.utils.ShareUtils;

public class RageFacePickerActivity extends Activity {

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

	@SuppressWarnings("unchecked")
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
				mRageFaceUri = ShareUtils.loadRageFace(mContext, mRageFaceName, getResId(mRageFaceName));

				Intent intent = getIntent();
				if (intent.getAction().equals(Intent.ACTION_GET_CONTENT)
						|| intent.getAction().equals(Intent.ACTION_PICK)) {
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
			Pair<String, Integer>[] categories = new Pair[len];
			int a = 0;
			while (!c.isAfterLast()) {
				int id = c.getInt(0);
				String category = c.getString(1);
				int resId = ResourceUtils.getResourceId(R.string.class, c.getString(1));
				if (resId != -1) {
					category = getString(resId);
				}

				categories[a] = Pair.create(category, id);

				c.moveToNext();
				a++;
			}
			c.close();
			db.close();

			// Sort the results (sort order can change based on language)
			Comparator<Pair<String, Integer>> sorter = new Comparator<Pair<String, Integer>>() {
				public int compare(Pair<String, Integer> lhs, Pair<String, Integer> rhs) {
					return lhs.first.compareTo(rhs.first);
				}
			};
			Arrays.sort(categories, sorter);

			// Fill the data into the category variables
			mCategoryIds = new int[len];
			mCategoryNames = new String[len];
			for (a = 0; a < len; a++) {
				mCategoryIds[a] = categories[a].second;
				mCategoryNames[a] = categories[a].first;
			}
		}

		// Load rage faces to SD card
		int errResId = ShareUtils.loadRageFacesDir(this);
		if (errResId == 0) {
			mLoadingContainer.setVisibility(View.GONE);
			mGridView.setVisibility(View.VISIBLE);
		}
		else {
			mMessageView.setText(errResId);
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

	private int getResId(String rageFaceName) {
		RawRetriever retriever = (RawRetriever) mAdapter;
		return retriever.getRawResourceId(rageFaceName);
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
			final ArrayList<Runnable> actions = new ArrayList<Runnable>();

			// Standard share option
			items.add(getString(R.string.dialog_actions_opt_share));
			actions.add(new Runnable() {
				public void run() {
					ShareUtils.shareRageFace(mContext, mRageFaceUri);
				}
			});

			// If the user has Sense messaging, that option
			if (ShareUtils.hasSenseMessagingApp(mContext, mRageFaceUri)) {
				items.add(getString(R.string.dialog_actions_opt_share_sense));
				actions.add(new Runnable() {
					public void run() {
						ShareUtils.shareRageFaceSenseMessaging(mContext, mRageFaceUri);
					}
				});
			}

			// View the face full screen
			items.add(getString(R.string.dialog_actions_opt_view));
			actions.add(new Runnable() {
				public void run() {
					Intent intent = new Intent(mContext, RageFaceViewerActivity.class);
					intent.putExtra(RageFaceViewerActivity.EXTRA_FACE_ID, mRageFaceId);
					startActivity(intent);
				}
			});

			// Add the face to the gallery
			items.add(getString(R.string.dialog_actions_opt_add_to_gallery));
			actions.add(new Runnable() {
				public void run() {
					String url = ShareUtils.addRageFaceToGallery(mContext, mRageFaceName, getResId(mRageFaceName));

					if (url != null) {
						Toast.makeText(mContext, R.string.added_face_to_gallery_toast, Toast.LENGTH_LONG).show();
					}
				}
			});

			Builder builder = new Builder(this);
			builder.setTitle(R.string.dialog_actions_title);
			builder.setItems(items.toArray(new String[0]), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					actions.get(which).run();
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

			String contributors = formatNameList(mResources.getStringArray(R.array.contributors));
			String translators = formatNameList(mResources.getStringArray(R.array.translators));

			Builder builder = new Builder(this);
			builder.setMessage(getString(R.string.about_msg, versionName, contributors, translators));
			builder.setNeutralButton(android.R.string.ok, null);
			return builder.create();
		}

		return super.onCreateDialog(id);
	}

	private String formatNameList(String[] names) {
		StringBuilder sb = new StringBuilder();
		for (int a = 0; a < names.length; a++) {
			if (a > 0) {
				sb.append(", ");
			}
			sb.append(names[a]);
		}
		return sb.toString();
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