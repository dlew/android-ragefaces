package com.idunnolol.ragefaces.test;

import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.database.sqlite.SQLiteDatabase;
import android.widget.BaseAdapter;

import com.idunnolol.ragefaces.adapters.RageFaceDbAdapter;

@RunWith(RobolectricTestRunner.class)
public class DbAdapterTest extends BaseAdapterTest {

	@Override
	protected BaseAdapter getAdapter() {
		SQLiteDatabase db = SQLiteDatabase.openDatabase("assets/faces.db",
				null, 0);
		RageFaceDbAdapter mAdapter = new RageFaceDbAdapter(Robolectric
				.getShadowApplication().getApplicationContext(), db);
		mAdapter.filter(null);
		return mAdapter;
	}

}
