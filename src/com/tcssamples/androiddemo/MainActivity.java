package com.tcssamples.androiddemo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.tcssamples.compass.CompassNorthProvider;

public class MainActivity extends FragmentActivity
		implements
			CompassNorthProvider.ChangeEventListener,
			GoogleApiClient.ConnectionCallbacks {

	private TextView mTxtCompassBearing;
	private CompassNorthProvider mCompassProvider;
	private ImageView mCompassPointer;
	private ImageView mThumbnail;

	static final int REQUEST_TAKE_PHOTO = 1;
	static final String PHOTO_PATH = "com.tcssamples.photoPath";
	private String mCurrentPhotoPath;

	private AutoCompleteTextView mSearchAutoCompleteText;
	private View mSearchView;
	ArrayAdapter<String> mAutoCompleteAdapter;

	private GoogleApiClient mGoogleApiClient;
	private PlaceArrayAdapter mPlaceArrayAdapter;
	private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
			new LatLng(8.06890, 68.03215), new LatLng(35.674520, 97.16712));

	private static final String LOG_TAG = "MainActivity";
	private static final int GOOGLE_API_CLIENT_ID = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		mTxtCompassBearing = (TextView) findViewById(R.id.txtCompassBearing);
		mCompassPointer = (ImageView) findViewById(R.id.imgCompassPointer);
		mThumbnail = (ImageView) findViewById(R.id.imgThumbnail);

		mCompassProvider = new CompassNorthProvider(this);
		mCompassProvider.setChangeEventListener(this);

		mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
				.addApi(Places.GEO_DATA_API)
				.addConnectionCallbacks(this)
				.enableAutoManage(this, GOOGLE_API_CLIENT_ID,
						connectionFailedListener).build();

	}

	private GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {

		@Override
		public void onConnectionFailed(ConnectionResult connectionResult) {
			Log.e(LOG_TAG,
					"Google Places API connection failed with error code: "
							+ connectionResult.getErrorCode());

			Toast.makeText(
					getApplicationContext(),
					"Google Places API connection failed with error code:"
							+ connectionResult.getErrorCode(),
					Toast.LENGTH_LONG).show();
		}
	};

	@Override
	protected void onPause() {
		// TODO Auto-generated method stubINDIA
		super.onPause();
		mCompassProvider.stop();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mCompassProvider.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		mSearchView = (View) menu.findItem(R.id.searchItem).getActionView();
		mSearchAutoCompleteText = (AutoCompleteTextView) mSearchView
				.findViewById(R.id.searchAutoCompleteTextView);
		mSearchAutoCompleteText.setThreshold(3);

		/*
		 * String[] placesInIndia = getResources().getStringArray(
		 * R.array.places_india); mAutoCompleteAdapter = new
		 * ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
		 * placesInIndia);
		 */

		mSearchAutoCompleteText
				.setOnItemClickListener(mAutoCompleteClickListener);
		mPlaceArrayAdapter = new PlaceArrayAdapter(this,
				android.R.layout.simple_list_item_1, BOUNDS_MOUNTAIN_VIEW, null);
		mSearchAutoCompleteText.setAdapter(mPlaceArrayAdapter);

		/*
		 * mSearchAutoCompleteText.setAdapter(mAutoCompleteAdapter);
		 * 
		 * mSearchAutoCompleteText .setOnItemClickListener(new
		 * OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> parent, View view,
		 * int position, long id) { Toast.makeText( MainActivity.this,
		 * mAutoCompleteAdapter.getItem(position) .toString(),
		 * Toast.LENGTH_SHORT).show(); } });
		 */

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.searchItem) {
			if (mSearchAutoCompleteText != null) {
				mSearchAutoCompleteText.requestFocus();
				InputMethodManager imm = (InputMethodManager) this
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mSearchAutoCompleteText,
						InputMethodManager.SHOW_FORCED);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBearingChanged(double bearing) {
		// TODO Auto-generated method stub
		mTxtCompassBearing.setText(String.format("Bearing : %f",
				(bearing + 360) % 360));
		mCompassPointer.setRotation((float) bearing - 30);

	}

	public void initiatePhotoCapture(View view) {
		dispatchTakePictureIntent();
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "JPEG_" + timeStamp + ".jpg";
		File storageDir = this.getExternalFilesDir(null);
		File image = new File(storageDir, imageFileName);

		// Save a file: path for use with ACTION_VIEW intents
		mCurrentPhotoPath = image.getAbsolutePath();
		return image;
	}

	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				// Error occurred while creating the File
				int a = 10;
			}

			// Continue only if the File was successfully created
			if (photoFile != null) {

				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));

				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		} else
			Toast.makeText(this,
					"There is no camera application on your phone",
					Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
			/*
			 * Bundle extras = data.getExtras(); Bitmap thumbnail = (Bitmap)
			 * extras.get("data"); mThumbnail.setImageBitmap(thumbnail);
			 */

			Intent initiateCapture = new Intent(this,
					PhotoCaptureActivity.class);
			initiateCapture.putExtra(PHOTO_PATH, mCurrentPhotoPath);
			startActivity(initiateCapture);

		}

	}

	private AdapterView.OnItemClickListener mAutoCompleteClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter
					.getItem(position);
			final String placeId = String.valueOf(item.placeId);
			Log.i(LOG_TAG, "Selected: " + item.description);
			PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
					.getPlaceById(mGoogleApiClient, placeId);
			placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
			Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);
		}
	};

	private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
		@Override
		public void onResult(PlaceBuffer places) {
			if (!places.getStatus().isSuccess()) {
				Log.e(LOG_TAG, "Place query did not complete. Error: "
						+ places.getStatus().toString());
				return;
			}
			// Selecting the first object buffer.
			final Place place = places.get(0);

			Toast.makeText(getApplicationContext(),
					place.getLatLng().toString(), Toast.LENGTH_SHORT).show();

		}
	};

	@Override
	public void onConnected(Bundle bundle) {
		mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
		Log.i(LOG_TAG, "Google Places API connected.");

	}

	@Override
	public void onConnectionSuspended(int i) {
		mPlaceArrayAdapter.setGoogleApiClient(null);
		Log.e(LOG_TAG, "Google Places API connection suspended.");
	}
}
