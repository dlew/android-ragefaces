package com.idunnolol.ragefaces.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.idunnolol.ragefaces.R;

public class PickerFragment extends Fragment {

	// Data being displayed
	private ListAdapter mAdapter;
	private CharSequence mMessage;
	private int mButtonBarVisibility = View.VISIBLE;

	// Cached views 
	private LinearLayout mLoadingContainer;
	private TextView mMessageView;
	private GridView mGridView;
	private ViewGroup mButtonBar;

	// If the system is using v11+ action bars, hide button bar
	private boolean mHideButtonBar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.main, container, false);

		mLoadingContainer = (LinearLayout) v.findViewById(R.id.LoadingContainer);
		mMessageView = (TextView) v.findViewById(R.id.Message);
		mGridView = (GridView) v.findViewById(R.id.GridView);
		mButtonBar = (ViewGroup) v.findViewById(R.id.button_bar_layout);

		// Configure the GridView
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onFaceClicked(position);
			}
		});

		// Configure the filter button
		Button filterButton = (Button) v.findViewById(R.id.filter_button);
		filterButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onFilterClicked();
			}
		});

		mHideButtonBar = getResources().getBoolean(R.bool.hide_filter_bar);
		if (mHideButtonBar) {
			mButtonBarVisibility = View.GONE;
		}

		// Load initial data
		loadMessage();
		loadFaces();
		loadButtonBar();

		return v;
	}

	public void displayFaces(ListAdapter adapter) {
		mAdapter = adapter;
		loadFaces();
	}

	private void loadFaces() {
		if (mGridView != null && mAdapter != null) {
			mGridView.setAdapter(mAdapter);
		}
	}

	public void displayMessage(CharSequence message) {
		mMessage = message;
		loadMessage();
	}

	private void loadMessage() {
		if (mMessageView != null) {
			if (mMessage != null && mMessage.length() > 0) {
				mLoadingContainer.setVisibility(View.VISIBLE);
				mGridView.setVisibility(View.GONE);
				mMessageView.setText(mMessage);
			}
			else {
				mLoadingContainer.setVisibility(View.GONE);
				mGridView.setVisibility(View.VISIBLE);
			}
		}
	}

	public void setButtonBarVisibility(int visibility) {
		if (mHideButtonBar) {
			mButtonBarVisibility = View.GONE;
		}
		else {
			mButtonBarVisibility = visibility;
		}

		loadButtonBar();
	}

	private void loadButtonBar() {
		if (mButtonBar != null) {
			mButtonBar.setVisibility(mButtonBarVisibility);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// PickerFragmentListener

	private PickerFragmentListener mListener;

	public void setListener(PickerFragmentListener listener) {
		mListener = listener;
	}

	private void onFaceClicked(int position) {
		if (mListener != null) {
			mListener.onFaceClicked(position);
		}
	}

	private void onFilterClicked() {
		if (mListener != null) {
			mListener.onFilterClicked();
		}
	}

	public interface PickerFragmentListener {
		public void onFaceClicked(int position);

		public void onFilterClicked();
	}
}
