package com.idunnolol.ragefaces;

import android.app.Activity;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.widget.Toast;

public class RageFaceMediaScanner implements MediaScannerConnectionClient {

	private Activity mActivity;
	private String mPath;
	private MediaScannerConnection mConn;

	public RageFaceMediaScanner(Activity activity, String path) {
		mActivity = activity;
		mPath = path;
		mConn = new MediaScannerConnection(activity, this);
	}

	public void doScan() {
		mConn.connect();
	}

	@Override
	public void onMediaScannerConnected() {
		mConn.scanFile(mPath, "image/png");
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mActivity, R.string.added_face_to_gallery_toast, Toast.LENGTH_LONG).show();
				mActivity = null;
			}
		});

		mConn.disconnect();
	}
}
