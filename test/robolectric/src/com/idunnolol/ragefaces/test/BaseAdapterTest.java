package com.idunnolol.ragefaces.test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowImageView;

import android.content.Context;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.idunnolol.ragefaces.R;

public abstract class BaseAdapterTest extends BaseTest {

	private Context mContext;
	private BaseAdapter mAdapter;

	protected abstract BaseAdapter getAdapter();

	@Before
	public void setUp() throws Exception {
		mContext = Robolectric.getShadowApplication().getApplicationContext();
		mAdapter = getAdapter();
	}

	@Test
	public void testAdapterResourcesExist() {
		int count = mAdapter.getCount();

		for (int a = 0; a < count; a++) {
			String resourceId = (String) mAdapter.getItem(a);
			try {
				R.drawable.class.getField(resourceId);
			} catch (NoSuchFieldException e) {
				fail("No such resource: " + resourceId);
			}
		}
	}

	@Test
	public void testAdapterGetView() throws Exception {
		int count = mAdapter.getCount();

		FrameLayout parent = new FrameLayout(mContext);
		for (int a = 0; a < count; a++) {
			View view = mAdapter.getView(a, null, parent);
			ShadowImageView shadowImageView = Robolectric
					.shadowOf((ImageView) view);
			assertThat(shadowImageView.getDrawable(), notNullValue());
		}
	}

}
