package com.jidasung.googlelogintestproject;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnPersonLoadedListener;
import com.google.android.gms.plus.model.people.Person;

public class MainActivity extends Activity implements View.OnClickListener, ConnectionCallbacks, OnConnectionFailedListener, OnPersonLoadedListener {

	private static final String TAG = "MainActivity";
	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

	private ProgressDialog mConnectionProgressDialog;
	private PlusClient mPlusClient;
	private ConnectionResult mConnectionResult;
	
	TextView profileName;
	String profile = "";
	ImageView profileImage;
	String imgurl = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// plusclient 를 통해 정보를 받아온다.
		mPlusClient = new PlusClient.Builder(this, this, this).setScopes(Scopes.PLUS_LOGIN)
						.setVisibleActivities("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity",
								"http://schemas.google.com/CreateActivity")
						.build();
		// Progress bar to be displayed if the connection failure is not resolved.
		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog.setMessage("Signing in...");
		
		setContentView(R.layout.activity_main);
		
		profileName = (TextView)findViewById(R.id.textView1);
		profileImage = (ImageView)findViewById(R.id.imageView1);
		findViewById(R.id.sign_in_button).setOnClickListener(this);
		findViewById(R.id.sign_out_button).setOnClickListener(this);
		
//		signinbutton = (SignInButton)findViewById(R.id.sign_in_button);
//		signinbutton.setOnClickListener(this);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		if(mConnectionProgressDialog.isShowing()) {
			// The user clicked the sign-in button already.
			// Start to resolve connection errors.
			// Wait until onConnected() to dismiss the connection dialog.
			if(result.hasResolution()) {
				try {
					result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
				} catch(SendIntentException e) {
					mPlusClient.connect();
				}
			}
		}
		
		// Save the intent so that we can start an activity when the user clicks the sign-in button.
		mConnectionResult = result;
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// TODO Auto-generated method stub
		mConnectionProgressDialog.dismiss();
		String accountName = mPlusClient.getAccountName();
		Toast.makeText(this, accountName + " is connected.", Toast.LENGTH_LONG).show();
		
		// 유저 자신의 person 객체 로드하기
		mPlusClient.loadPerson(this, "me");
		
		// 프로필 이름 받아오기
		profile = mPlusClient.getCurrentPerson().getDisplayName();
		profileName.setText(profile);

		// 프로필 이미지 받아오기
		imgurl = mPlusClient.getCurrentPerson().getImage().getUrl();
		ConnectImage mConnectImage = new ConnectImage();
		mConnectImage.execute(imgurl);
		Drawable mDrawable = null;
		try {
			mDrawable = mConnectImage.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		profileImage.setImageDrawable(mDrawable);
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		Log.d(TAG, "disconnected");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode == REQUEST_CODE_RESOLVE_ERR && resultCode == RESULT_OK) {
			mConnectionResult = null;
			// Try Connect Again.
			mPlusClient.connect();
		}
	}
	
	

	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.sign_in_button && !mPlusClient.isConnected()) {
			if(mConnectionResult == null) {
				mConnectionProgressDialog.show();
			} else {
				try {
					mConnectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
				} catch(SendIntentException e) {
					mConnectionResult = null;
					mPlusClient.connect();
				}
			}
		}
		if(v.getId() == R.id.sign_out_button) {
			if(mPlusClient.isConnected()) {
				mPlusClient.clearDefaultAccount();
				mPlusClient.disconnect();
				mPlusClient.connect();
				
				profileName.setText("No User");
				profileImage.setImageResource(R.drawable.common_signin_btn_text_disabled_focus_light);
			}
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		mPlusClient.connect();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mPlusClient.disconnect();
	}
	
	
	// person 객체가 로드되면 ..콜백함수
	@Override
	public void onPersonLoaded(ConnectionResult status, Person person) {
		// TODO Auto-generated method stub
		if(status.getErrorCode() == ConnectionResult.SUCCESS) {
			Log.d(TAG,"Display Name : " + person.getImage().getUrl());
//			Toast.makeText(this, "Display Name : "+person.getImage().getUrl(), Toast.LENGTH_LONG).show();
		}
	}

	// 프로필 이미지를 받아오는 asynctask 클래스
	private class ConnectImage extends AsyncTask<String, Integer, Drawable> {

		@Override
		protected Drawable doInBackground(String... params) {
			// TODO Auto-generated method stub
			String url = params[0];
			url = url.replace("sz=50", "sz=600");			
			try {
				InputStream is = (InputStream)new URL(url).getContent();
				Drawable d = Drawable.createFromStream(is, "src name");
				return d;
			} catch (Exception e) {
				Log.e("MainActivity",e.toString());
				return null;
			}
		}
		
	}
}



