package com.tcssamples.androiddemo;

import java.io.File;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

public class PhotoCaptureActivity extends Activity {

	private String mPhotoPath;
	private ImageView mPhotoView;
	private Uri mPhotoUri;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_capture);

		mPhotoView = (ImageView) findViewById(R.id.imgCapturedPhoto);
		Intent passedIntent = getIntent();
		mPhotoPath = passedIntent.getStringExtra(MainActivity.PHOTO_PATH);
		mPhotoUri = Uri.fromFile(new File(mPhotoPath));

		mContext = this;

		new PictureLoader().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.photo_capture, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void addToGalleryDB() {
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.DATA, mPhotoPath);
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	}

	public class PictureLoader extends AsyncTask<Void, Void, Void> {

		Bitmap bitmap = null;
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			getContentResolver().notifyChange(mPhotoUri, null);
			ContentResolver cr = getContentResolver();

			try {
				bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr,
						mPhotoUri);

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub

			try {
				if (bitmap != null) {

					Toast.makeText(mContext, mPhotoUri.toString(),
							Toast.LENGTH_LONG).show();

					mPhotoView.setImageBitmap(bitmap);
					addToGalleryDB();
					// bitmap.recycle();
				} else
					Toast.makeText(mContext, "Failed to load",
							Toast.LENGTH_SHORT).show();

			} catch (Exception exp) {
				int a = 10;
			}
		}
	}
}
