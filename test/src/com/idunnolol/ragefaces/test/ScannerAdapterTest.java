package com.idunnolol.ragefaces.test;

import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.widget.BaseAdapter;

import com.idunnolol.ragefaces.adapters.RageFaceScannerAdapter;

@RunWith(RobolectricTestRunner.class)
public class ScannerAdapterTest extends BaseAdapterTest {

	@Override
	protected BaseAdapter getAdapter() {
		return new RageFaceScannerAdapter(Robolectric.getShadowApplication()
				.getApplicationContext());
	}
}
