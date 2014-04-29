package com.MyTestApp.pebbletest;

import static com.getpebble.android.kit.Constants.KIT_STATE_COLUMN_CONNECTED;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.common.primitives.UnsignedInteger;

import android.R.bool;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SleepStreamActivity extends Activity {
	
	//Receiver for AppSync - Retrieves Dictionaries from Pebble app
	private PebbleKit.PebbleDataReceiver myDataReceiver;
	//Receiver for Data Logging - Retrieves Data Logs from Pebble app
	private PebbleKit.PebbleDataLogReceiver mDataLogReceiver = null;

	// REPLACE THE UUID OF THE PEBBLE APP UUID. THE FORMAT HAS TO BE CORRECT OR
	// THE APP WILL CRASH //
	private final static String sUUID = "84f85a90-26e3-42bd-881c-c5fc3f5a5ce5";
	private final static UUID PEBBLE_APP_UUID = UUID.fromString(sUUID);
	// ==============================================================================================
	
	
	static final int MUSIC_REQUEST_CODE = 1;
	
	// Dictionary keys. 0 = play music. 1 = stop music.
	private final int MUSIC_TRIGGER_KEY = 0;
	private final int START_MUSIC_VALUE = 1;
	private final int STOP_MUSIC_VALUE = 2;
	
	//Web server info
	final String webServerURL_GET = "https://sleepstream.herokuapp.com/api/v1/users/";
	final String webServerURL_POST_InitiateLogin = "https://sleepstream.herokuapp.com/api/v1/sessions";
	final String webServerURL_POST_InitiateSleep = "https://sleepstream.herokuapp.com/api/v1/sleeps";
	final String webServerURL_PUT = "https://sleepstream.herokuapp.com/api/v1/sleeps/";
	final String URL_TOKEN_PREFIX = "Token ";
	
	private final String CURRENT_ACCOUNT_ID_STORAGE = "userAccountIdStorage";
	private final String CURRENT_ACCOUNT_TOKEN_STORAGE = "TokenStorage";
	private final String CURRENT_ACCOUNT_LATEST_SLEEP_ID_STORAGE = "latestSleepIDRequested";
	
	
	private final String STORED_MUSIC_PREFERENCES = "SharedPreferencesMusic";
	private final String MUSIC_LENGTH_KEY = "musicLimitation";
	private final String SAVED_USER_ACCOUNT_INFO = "SharedPreferences_UserAccount";
	private final String NO_MUSIC_FOUND_ERROR_STRING = "No music chosen. Please select a song.";
	
	//Info for music max duration playback
	private final int MUSIC_MAX_LENGTH = 20000;
	MyCountDownTimerClass myCountDownTimerVariable;
	AsyncTask myAsyncTaskVariable;
	
	//AsyncTask Result Codes
	private final int PUT_REQUEST_SUCCESS = 1000;
	private final int PUT_REQUEST_FAILED = 2000;
	private final int HTTP_ERROR = 3000;
	private final int SERVER_OFFLINE = 4000;
	
	SharedPreferences spSleepMusicSettings;
	SharedPreferences spUserAccountInformation;
	SharedPreferences.Editor editorForUserAccountInformation;
	SharedPreferences.Editor editorForMusicSettings;
	
	String sShowStoredmusic;
	Button bMusicDurationButton;
	TextView tvMusicDisplayer;
	TextView tvParsedJSON;
	String sRetrievedMusicPath;
	MediaPlayer mpMusicPlayer;
	
	@Override
	public void onBackPressed() 
	{
		mLogoutCodeBlock();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Usually: Always deregister any Activity-scoped BroadcastReceivers when the Activity is paused
		/*
		
		if (myDataReceiver != null) {
			unregisterReceiver(myDataReceiver);
			myDataReceiver = null;
		}
		
        if (mDataLogReceiver != null) {
            unregisterReceiver(mDataLogReceiver);
            mDataLogReceiver = null;
        }
		
		*/
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (mpMusicPlayer != null) {
			if (mpMusicPlayer.isPlaying()) {
				mpMusicPlayer.stop();
			}
			mpMusicPlayer.reset();
			mpMusicPlayer.release();
			mpMusicPlayer = null;

		}
		
		////////////////// Only stop receiving data from Pebble when app is destroyed ///////////////////
		if (myDataReceiver != null) {
			unregisterReceiver(myDataReceiver);
			myDataReceiver = null;
		}
		
        if (mDataLogReceiver != null) {
            unregisterReceiver(mDataLogReceiver);
            mDataLogReceiver = null;
        }
        ///////////////// /Only stop receiving data from Pebble when app is destroyed ///////////////////
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sleepstream);
		
		mpMusicPlayer = new MediaPlayer();
		
		// Display which music is music is currently stored in SharedPreferences
		updateMusicDisplayed();
		bMusicDurationButton = (Button) findViewById(R.id.bChooseMusicDuration);
		spSleepMusicSettings = getSharedPreferences(STORED_MUSIC_PREFERENCES, MODE_PRIVATE);
		spUserAccountInformation = getSharedPreferences(SAVED_USER_ACCOUNT_INFO, MODE_PRIVATE); 
		mInitialSongLimitSetText();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		
		return true;
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		//final Handler handler = new Handler();

		// To receive data back from the watch-app, Android
		// applications must register a "DataReceiver" to operate on the
		// dictionaries received from the watch.
		
		// ------------- Receive Dictionary Data ------------------------------------------------------------------------
		myDataReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) 
		{
			@Override
			public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) 
			{
				PebbleKit.sendAckToPebble(context, transactionId);

				// Get the value of the Dictionary with key 0 (MUSIC_TRIGGER) and store it in the variable receivedDictionaryData
				
				int iSongControllerValue = data.getInteger(MUSIC_TRIGGER_KEY).intValue();
				
				if(iSongControllerValue == START_MUSIC_VALUE)
				{

					//final int receivedDictionaryData = data.getInteger(MUSIC_TRIGGER_KEY).intValue();

					
					/*
					makeToast("Pebble Treshhold reached!\nValue of received Dictionary is: "
								+ receivedDictionaryData);
					*/
					
					try {
	
							if (mpMusicPlayer.isPlaying()) 
							{
								return;
							} 
							else if((sRetrievedMusicPath == null) || (sRetrievedMusicPath.equals("Could not retrieve music path")))
							{
								return;
							}
							else	
							{
								mpMusicPlayer.reset();
								mpMusicPlayer.setDataSource(sRetrievedMusicPath);
								// makeToast("MusicPath is:\n" + sRetrievedMusicPath);
								mpMusicPlayer.prepare();
								makeToast("Large movements detected! Song playing");
								mpMusicPlayer.start();
								
								if (spSleepMusicSettings.getBoolean(MUSIC_LENGTH_KEY, false) == true)
							{
									deployMusicTimer();
							}
								
						}
	
					} catch (Exception e) 
					{
						makeToast("Caught Exception Error in media player");
						// e.printStackTrace();
					}

				}
				else if(iSongControllerValue == STOP_MUSIC_VALUE)
				{
					//Stop music
					
					if(sShowStoredmusic == NO_MUSIC_FOUND_ERROR_STRING)
					{
						makeToast("Please choose a song first");
						return;
					}
					
					if (mpMusicPlayer.isPlaying()) 
					{	
						// Cancel current timer
						 if (myAsyncTaskVariable != null)
						 {
							 myAsyncTaskVariable.cancel(true);
						 }
						
						mpMusicPlayer.stop();
						mpMusicPlayer.reset();
						
						//Remove any timers
						 if (myCountDownTimerVariable != null)
						 {
							 myCountDownTimerVariable.cancel();
						 }
						 
						 makeToast("Pebble device stopped music");
						 
					}
					else
					{
						return;
					}
					
					
				}
				else
				{
					return;
				}
				
			}
		};
		PebbleKit.registerReceivedDataHandler(this, myDataReceiver);
		// ---------------------------------------------------------------------------------------------------
		
		
		// =============== Receive Data Logs =================================================================
		
        mDataLogReceiver = new PebbleKit.PebbleDataLogReceiver(PEBBLE_APP_UUID) 
        {
            @Override
            public void receiveData(Context context, UUID logUuid, UnsignedInteger timestamp, UnsignedInteger tag, int receivedInt) 
            {
            	//makeToast("DatalogReceived with following info:\nlogUUID: " + logUuid + "\nTimestamp: " + timestamp + "\nTag: " + tag + "\nReceivedInt: " + receivedInt);            	
            	makeToast("Pebble Accelerometer:\n" + "Magnitude of largest movement this interval was " + receivedInt);  
            	
        		// If token, user ID or sleep ID cannot be found in SharedPreferences, do not perform PUT request to web server
        		if((spUserAccountInformation.contains(CURRENT_ACCOUNT_ID_STORAGE) && spUserAccountInformation.contains(CURRENT_ACCOUNT_TOKEN_STORAGE) && spUserAccountInformation.contains(CURRENT_ACCOUNT_LATEST_SLEEP_ID_STORAGE) == false))
        		{
        			//makeToast("Could not retrieve token, user ID, or latest sleep ID");
        			makeToast("Movements not sent to web server: Could not authorize user or find an active sleep session");
        			return;
        		}


            	new AsyncTaskPUTRequestWebServer().execute(receivedInt);
            	
            }

        };
		
        PebbleKit.registerDataLogReceiver(this, mDataLogReceiver);

        PebbleKit.requestDataLogsForApp(this, PEBBLE_APP_UUID);
        
        // =============== Receive Data Logs =================================================================
		
	}

	public void mStartCommunication(View v) {
		// Buttontester
		// makeToast("Start Button Works!!");

		boolean bPebbleConnection = PebbleKit
				.isWatchConnected(getApplicationContext());
		if (bPebbleConnection == true) {
			makeToast("Pebble Connection Detected");
		} else {
			makeToast("No Pebble Detected Nearby");
		}

	}

	// Start new ActivityForResult to let user choose music.
	public void mChooseMusic(View v) {
		// makeToast("Button Works!!");
		
		
		if (myDataReceiver != null) {
			unregisterReceiver(myDataReceiver);
			myDataReceiver = null;
		}
		
        if (mDataLogReceiver != null) {
            unregisterReceiver(mDataLogReceiver);
            mDataLogReceiver = null;
        }
		
		
		if (mpMusicPlayer != null) 
		{
			if (mpMusicPlayer.isPlaying()) 
			{
				mpMusicPlayer.stop();
			}
			mpMusicPlayer.reset();
		}

		Intent intent = new Intent(this, AudiolistActivityExternalStorage.class);
		startActivityForResult(intent, MUSIC_REQUEST_CODE);
	}

	// When User has finished choosing music, identify it from sharedpreferences.
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == MUSIC_REQUEST_CODE) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				updateMusicDisplayed();
			}
		}
	}

	public void mLaunchPebbleApp(View v) 
	{
		boolean bPebbleConnection = PebbleKit
				.isWatchConnected(getApplicationContext());
		if (bPebbleConnection == true) 
		{
			PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
			//makeToast("Launching Pebble App with UUID:\n" + sUUID);
			makeToast("Launching Pebble App");
		} else {
			makeToast("No Pebble Detected Nearby");
		}
		
;		 
		

	}

	public void mKillPebbleApp(View v) 
	{
		
		boolean bPebbleConnection = PebbleKit
				.isWatchConnected(getApplicationContext());
		if (bPebbleConnection == true) {
			PebbleKit.closeAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
			//makeToast("Terminating Pebble App with UUID:\n" + sUUID);
			makeToast("Terminating Pebble App");
		} else {
			makeToast("No Pebble Detected Nearby");
		}	
		
		
	}
	
	/* --------------------- sending dictionary data to Pebble device not used ----------------------
	public void mSendSampleDictionary(View v) {
		// make PebbleDictionary
		PebbleDictionary data = new PebbleDictionary();
		// Insert data to PebbleDictionary
		data.addString(1, "sampleValue with Key 1");
		// Send PebbleDictionary with data to Pebble app with corresponding UUID
		PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID,
				data);
		makeToast("sample PebbleDictionary sent to Pebble App with UUID:\n"
				+ sUUID);
	}
	--------------------------------------------------------------------------------------------- */
	
	
	// Toastmaker
	private void makeToast(String s) 
	{
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, s, duration);
		toast.show();
	}
	
	private void mInitialSongLimitSetText()
	{
		{
			boolean boolDurationLimited = spSleepMusicSettings.getBoolean(MUSIC_LENGTH_KEY, false);
				
			if (boolDurationLimited == false) ////When music length limiter is off
			{
				bMusicDurationButton.setText("Enable 20 sec limit playback");	
			}
			else ////When music length limiter is on
			{
				bMusicDurationButton.setText("Disable 20 sec limit playback");
			}

		}
	}
	
	private void mLogoutCodeBlock()
	{
		editorForUserAccountInformation = spUserAccountInformation.edit();
		editorForMusicSettings = spSleepMusicSettings.edit();
		
		editorForMusicSettings.clear();
		editorForUserAccountInformation.clear();
		
		editorForMusicSettings.commit();
		editorForUserAccountInformation.commit();
		
		this.finish();
	}

	// Update which music is stored in sharedpreferences and display it on specified textview
	private void updateMusicDisplayed() 
	{
		spSleepMusicSettings = getSharedPreferences(STORED_MUSIC_PREFERENCES,
				MODE_PRIVATE);
		sShowStoredmusic = spSleepMusicSettings.getString("MusicName",
				NO_MUSIC_FOUND_ERROR_STRING);
		sRetrievedMusicPath = spSleepMusicSettings.getString("MusicPath",
				"Could not retrieve music path");
		tvMusicDisplayer = (TextView) findViewById(R.id.tvCurrentMusic);
		tvMusicDisplayer.setText("Current music file is:\n" + sShowStoredmusic);
		// makeToast("Music path is:\n" + sRetrievedMusicPath);
	}
	
	///////// Limit music to 20 seconds ///////////////////////////////////////
	 private void deployMusicTimer()
	{
		 if (myCountDownTimerVariable != null)
		 {
			 myCountDownTimerVariable.cancel();
		 }
		 myCountDownTimerVariable = new MyCountDownTimerClass(MUSIC_MAX_LENGTH, MUSIC_MAX_LENGTH);
		 myCountDownTimerVariable.start();
	}
	 
	 // ----------------------- Timer Class ---------------------------------------------
	 public class MyCountDownTimerClass extends CountDownTimer
	 {
		    public MyCountDownTimerClass(long millisInFuture, long countDownInterval) 
		    {
		    	super(millisInFuture, countDownInterval);
		    }
		    
		    @Override
		    public void onFinish() 
		    {
		    	//Conditions in order for timer to stop music 1: mediaplayer is not empty, 2: song is playing, 3: song has played for 20 seconds or more. Else, just let timer die.
		    	if ((mpMusicPlayer != null) && (mpMusicPlayer.isPlaying() == true) && (mpMusicPlayer.getCurrentPosition() >= MUSIC_MAX_LENGTH))
		    	{
		    		
		    		myAsyncTaskVariable = new FadeOutMusic().execute();
		    		
			    	}
		    	else
		    	{
		    		return;
		    	}
		    }
		    
		    @Override
		    public void onTick(long millisUntilFinished) 
		    {

		    }
		    
	 }
	 // -------------------------- Timer Class --------------------------------------------
	 
	 
	 // ------------------ Fade out of song performed in background. Called when CountDownTimer successfully finishes ----------------------------------------
	 public class FadeOutMusic extends AsyncTask<Void,Void,Void> 
	 {
		 
		  @Override
		  protected Void doInBackground(Void... args) 
		  {

			  		
	    		AudioManager myAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
	    		int volume_level = myAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			  
	    		float level = new Float(volume_level);
	    	    int i = 1;
	    	    // Fade out effect: Reduce song volume 100 times in intervals of 100ms. Each cycle reduces volume by 5%. Then set volume to 0, stop music and set volume to normal. 
	    	    while(i<100)
	    	    {
	    	    	//On every iteration, first detect if stop button has been pressed. If so, go to onCancelled()
				  	if(isCancelled() == true)
				  	{
				  		break;
				  	}	
	    	    	
				  		
		    	        i++;
		    	        level=level*0.95f;
		    	        mpMusicPlayer.setVolume(level, level);
		    	        
		    	        try 
		    	        {
		    	          Thread.sleep(100);
		    	        } 
		    	        catch (InterruptedException e) 
		    	        {
		    	          e.printStackTrace();
		    	        }
		    	        
	    	    }
		    
		    return null;
		  }

		  @Override
		  protected void onPostExecute(Void result) 
		  {
			// When Fade out loop finishes, reset music and restore volume
		    if(mpMusicPlayer != null)
		    {
		    	mpMusicPlayer.setVolume(0,0);      
			    mpMusicPlayer.stop();
			    mpMusicPlayer.reset();
			    mpMusicPlayer.setVolume(1, 1);
		    }
		  }

		@Override
		protected void onCancelled() 
		{
		    if(mpMusicPlayer != null)
		    {
			super.onCancelled();
			//If music is cancelled, restore volume to normal and continue with rest of stop code
		    mpMusicPlayer.setVolume(1, 1);
		    }
		} 
		  
		  
		  
		  
	}
	// ------------------ Fade out of song performed in background. Called when CountDownTimer successfully finishes ----------------------------------------
	 
	 
	//Play music button
	public void mPlayMusic(View v) {

		try 
		{
			if(sShowStoredmusic == NO_MUSIC_FOUND_ERROR_STRING)
			{
				makeToast("Please choose a song first");
				return;
			}
			
			if (mpMusicPlayer.isPlaying()) 
			{
				// Cancel current timer
				 if (myAsyncTaskVariable != null)
				 {
					 myAsyncTaskVariable.cancel(true);
				 }
				
				mpMusicPlayer.stop();
				mpMusicPlayer.reset();
				
				//Remove any timers
				 if (myCountDownTimerVariable != null)
				 {
					 myCountDownTimerVariable.cancel();
				 }
				 

				
			} 
			else 
			{
				mpMusicPlayer.reset();
				mpMusicPlayer.setDataSource(sRetrievedMusicPath);
				// makeToast("MusicPath is:\n" + sRetrievedMusicPath);
				mpMusicPlayer.prepare();
				mpMusicPlayer.start();
				
				if (spSleepMusicSettings.getBoolean(MUSIC_LENGTH_KEY, false) == true)
			{
					deployMusicTimer();
			}
				
			}

		} catch (Exception e) {
			makeToast("Media Player Error");
			// e.printStackTrace();
		}

	}
	
	public void mChooseMusicInternal(View v)
	{
		//makeToast("Button works!");
		
		if (myDataReceiver != null) {
			unregisterReceiver(myDataReceiver);
			myDataReceiver = null;
		}
		
        if (mDataLogReceiver != null) {
            unregisterReceiver(mDataLogReceiver);
            mDataLogReceiver = null;
        }
		
		
		
		if (mpMusicPlayer != null) 
		{
			if (mpMusicPlayer.isPlaying()) 
			{
				mpMusicPlayer.stop();
			}
			mpMusicPlayer.reset();
		}

		Intent intent = new Intent(this, AudiolistActivityInternalStorage.class);
		startActivityForResult(intent, MUSIC_REQUEST_CODE);
		
		
		
	}
	
	public void mWebServerActivity(View v)
	{
		//Stop receiving data from pebble
		if (myDataReceiver != null) {
			unregisterReceiver(myDataReceiver);
			myDataReceiver = null;
		}
		
        if (mDataLogReceiver != null) {
            unregisterReceiver(mDataLogReceiver);
            mDataLogReceiver = null;
        }
		
		
		
		//Stop music
		if (mpMusicPlayer != null) 
		{
			if (mpMusicPlayer.isPlaying()) 
			{
				mpMusicPlayer.stop();
			}
			mpMusicPlayer.reset();
		}
		
		//Go to Web Server Activity
		Intent intent = new Intent(this, WebServerActivity.class);
		startActivity(intent);
	}
	
	// Toggle music duration ----------------------------------------------------------------
	public void mSwitchMusicDuration(View v)
	{
		{
			boolean boolLimitedDuration = spSleepMusicSettings.getBoolean(MUSIC_LENGTH_KEY, false);
			
			SharedPreferences.Editor MusicDurationEditor = spSleepMusicSettings.edit();
			
			
			if (boolLimitedDuration == false) ////When music length limiter is off
			{
				MusicDurationEditor.putBoolean(MUSIC_LENGTH_KEY, true);
				MusicDurationEditor.commit();
				bMusicDurationButton.setText("Disable 20 sec limit playback");
				makeToast("Now set to play first 20 seconds of song. Replay to take effect.");
				
			}
			else ////When music length limiter is on
			{
				MusicDurationEditor.putBoolean(MUSIC_LENGTH_KEY, false);
				MusicDurationEditor.commit();
				bMusicDurationButton.setText("Enable 20 sec limit playback");
				makeToast("Now set to play entire song. Replay to take effect.");
			}

		}
		
	}
	// Toggle music duration ----------------------------------------------------------------
	
	//On log out, clear stored user information, and music preferences
	public void mLogoutUser(View v)
	{
		mLogoutCodeBlock();
	}
	
	
	private class AsyncTaskPUTRequestWebServer extends AsyncTask<Integer,Void,Integer>
	{
		
		
		@Override
		protected void onPreExecute() 
		{
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Integer... params) 
		{
			
			String iMovementValue = Integer.toString(params[0]);
			
			//int sleepIDToConvert = spUserAccount.getString(CURRENT_ACCOUNT_LATEST_SLEEP_ID_STORAGE, ERROR_STRING);
			//String sSleepIDConverted = Integer.toString(sleepIDToConvert);
			String webServerURLAppendedWithSleepID = webServerURL_PUT + spUserAccountInformation.getString(CURRENT_ACCOUNT_LATEST_SLEEP_ID_STORAGE, "Could not find sleep ID");
			
			
			HttpClient myHTTP_Client = new DefaultHttpClient();
			HttpPut myHTTP_Put = new HttpPut(webServerURLAppendedWithSleepID);
			
			try {				
				
				JSONObject jsonObject_putRequest = new JSONObject();
				try 
				{
				    // adding some keys/pair values in JSON object to send. value of "content" is sent to server along with sleep id request.
					jsonObject_putRequest.put("content", iMovementValue);
					
					
				}
				catch (Exception e)
				{
					
					Log.e("jsonObject_putRequest() Error: ",Log.getStackTraceString(e));
				}
			

				myHTTP_Put.setEntity(new StringEntity(jsonObject_putRequest.toString()));
				//--------------- Set correct Header for PUT request for JSON contents ------------------------------------------
				myHTTP_Put.setHeader("Content-type", "application/json");
				myHTTP_Put.setHeader("Accept", "application/json");
				myHTTP_Put.setHeader("Authorization" , URL_TOKEN_PREFIX + spUserAccountInformation.getString(CURRENT_ACCOUNT_TOKEN_STORAGE, "ERROR-COULD-NOT-FIND-TOKEN"));
				//-----------------------------------------------------------------------------------------------
				

				
				//Pass HttpPut to httpClient, execute and listen for server response
				HttpResponse myResponse = myHTTP_Client.execute(myHTTP_Put);
				//Pass response to a statusLine variable
				StatusLine slResponse = myResponse.getStatusLine();
				//Returned statusCode of web server
				int statusCode = slResponse.getStatusCode();
				
				//StatusCode 200 => Success
				if (statusCode == 200) 
				{
				// On success, pass response to an entity variable
					
					BufferedReader rd = new BufferedReader(
					        new InputStreamReader(myResponse.getEntity().getContent()));
				 
					StringBuffer result = new StringBuffer();
					String line = "";
					while ((line = rd.readLine()) != null) 
					{
						result.append(line);
						
					}
					
					String responseString = result.toString();			
					
					//JSON
					JSONObject jsonObject = new JSONObject(responseString);
					
					
					
				} 
				else //If statusCode is not 200: Server offline
				{
					return SERVER_OFFLINE;
				}
				
				} 
			catch (Exception e) 
			{
			Log.e("SleepStreamActivity HTTTP Error: ",Log.getStackTraceString(e));
				return HTTP_ERROR;			
			}
			
			// Pass PUT_REQUEST_SUCCESS to onPostExecute()
			return PUT_REQUEST_SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) 
		{
			super.onPostExecute(result);
			
			
			if (result == PUT_REQUEST_SUCCESS)
			{
				makeToast("Successfully uploaded accelerometer data to web server");
			}
			else if (result == SERVER_OFFLINE)
			{
				makeToast("Error: Web Server not responding");
			}
			else
			{
				makeToast("HTTP ERROR");
			}
			
		}
		
		
		
	}
	
	
}// End SleepStreamActivity Class
