package com.idunnolol.ragefaces.adapters;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.danlew.utils.ResourceUtils;
import com.idunnolol.ragefaces.R;
import com.idunnolol.ragefaces.data.Cache;
import com.idunnolol.ragefaces.data.DatabaseHelper;

public class RageFaceDbAdapter extends BaseAdapter {

	private Resources mResources;
	private LayoutInflater mInflater;

	private SQLiteDatabase mDb;
	private Cursor mCursor;

	public RageFaceDbAdapter(Context context) {
		mResources = context.getResources();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDb = DatabaseHelper.getFacesDb(context);
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
		int resId = ResourceUtils.getIdentifier(R.drawable.class, (String) getItem(position));
		imageView.setImageBitmap(Cache.getBitmap(mResources, resId));

		return imageView;
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
