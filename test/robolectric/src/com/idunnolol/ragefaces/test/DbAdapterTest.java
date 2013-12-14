package com.idunnolol.ragefaces.test;

import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.content.Context;
import android.widget.BaseAdapter;

import com.idunnolol.ragefaces.adapters.RageFaceDbAdapter;
import com.idunnolol.ragefaces.data.DatabaseHelper;

@RunWith(RobolectricTestRunner.class)
public class DbAdapterTest extends BaseAdapterTest {

	@Override
	protected BaseAdapter getAdapter() {
		Context context = Robolectric.getShadowApplication().getApplicationContext();
		DatabaseHelper.createOrUpdateDatabase(context);
		RageFaceDbAdapter mAdapter = new RageFaceDbAdapter(context);
		mAdapter.filter(null);
		return mAdapter;
	}

}
