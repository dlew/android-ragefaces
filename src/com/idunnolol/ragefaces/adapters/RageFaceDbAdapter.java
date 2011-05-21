package com.idunnolol.ragefaces.adapters;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.idunnolol.ragefaces.R;
import com.idunnolol.ragefaces.RageFacesApp;
import com.idunnolol.ragefaces.R.raw;
import com.idunnolol.ragefaces.data.DatabaseHelper;

public class RageFaceDbAdapter extends BaseAdapter implements RawRetriever {

	private LayoutInflater mInflater;

	private SQLiteDatabase mDb;
	private Cursor mCursor;

	// This is just used to speed up the app
	private HashMap<String, Integer> mResourceIds;

	public RageFaceDbAdapter(Context context) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDb = DatabaseHelper.getFacesDb(context);
		mResourceIds = new HashMap<String, Integer>();

		filter(null);
	}

	@Override
	public int getCount() {
		return mCursor.getCount();
	}

	@Override
	public Object getItem(int position) {
		mCursor.moveToPosition(position);
		return mCursor.getString(1);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public long getItemId(int position) {
		mCursor.moveToPosition(position);
		return mCursor.getInt(0);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.face, parent, false);
		}

		ImageView imageView = (ImageView) convertView;
		imageView.setImageResource(getRawResourceId((String) getItem(position)));

		return imageView;
	}

	@Override
	public int getRawResourceId(String name) {
		if (mResourceIds.containsKey(name)) {
			return mResourceIds.get(name);
		}

		try {
			Class<raw> res = R.raw.class;
			Field field = res.getField(name);
			int resId = field.getInt(null);
			mResourceIds.put(name, resId);
			return resId;
		}
		catch (Exception e) {
			Log.e(RageFacesApp.TAG, "Failure to get raw id.", e);
			return -1;
		}
	}

	public void filter(List<Integer> categories) {
		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
		}

		mCursor = DatabaseHelper.getFaces(mDb, categories);
		notifyDataSetChanged();
	}

	public void shutdown() {
		mCursor.close();
		mDb.close();
	}
}
