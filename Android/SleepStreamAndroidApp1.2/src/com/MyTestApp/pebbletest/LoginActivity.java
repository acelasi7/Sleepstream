package com.MyTestApp.pebbletest;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity 
{	
	// UI elements
	TextView tvEmailForm;
	TextView tvPasswordForm;
	Button bLoginUserButton;
	Button bRegisterUserButton;
	ProgressBar progressBarLoginActvity;
	
	//SharedPreferences info
	SharedPreferences spUserAccountLoginActivity; 
	SharedPreferences.Editor editorForUserAccountLoginActivity;
	SharedPreferences.Editor editorForUserAccountLoginActivity2;
	private final String SAVED_USER_ACCOUNT_INFO = "SharedPreferences_UserAccount";
	private final String CURRENT_ACCOUNT_TOKEN_STORAGE = "TokenStorage";
	private final String CURRENT_ACCOUNT_ID_STORAGE = "userAccountIdStorage";
	private final String CURRENT_ACCOUNT_LATEST_SLEEP_ID_STORAGE = "latestSleepIDRequested";
	private final int ERROR_STRING = 999999999;
	
	
	//Web server info
	private final String sRegisterURI = "https://sleepstream.herokuapp.com/signup";
	final String webServerURL_GET = "https://sleepstream.herokuapp.com/api/v1/users/";
	final String webServerURL_POST_InitiateLogin = "https://sleepstream.herokuapp.com/api/v1/sessions";
	final String webServerURL_POST_InitiateSleep = "https://sleepstream.herokuapp.com/api/v1/sleeps";
	final String webServerURL_PUT = "https://sleepstream.herokuapp.com/api/v1/sleeps/";
	final String URL_TOKEN_PREFIX = "Token ";
	
	//AsyncTask Result Codes
	private final int LOGIN_SUCCESS = 1000;
	private final int LOGIN_FAILED = 2000;
	private final int HTTP_ERROR = 3000;
	private final int SERVER_OFFLINE = 4000;

  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    
    tvEmailForm = (TextView) findViewById(R.id.etEmailEntry);
    tvPasswordForm = (TextView) findViewById(R.id.etPasswordEntry);
    
    bLoginUserButton = (Button) findViewById(R.id.bLoginAccount);
    bRegisterUserButton = (Button) findViewById(R.id.bRegisterAccount);
    
    progressBarLoginActvity = (ProgressBar) findViewById(R.id.progressBarLoginActvity);
    
    spUserAccountLoginActivity = getSharedPreferences(SAVED_USER_ACCOUNT_INFO, MODE_PRIVATE);
    
	// If there is already a stored token for current user, skip the login activity
    if (spUserAccountLoginActivity.contains(CURRENT_ACCOUNT_TOKEN_STORAGE)) 
    {
		Intent intent = new Intent(this, SleepStreamActivity.class);
		startActivity(intent);
    } 
  }
  
  
  
  //Toastmaker
  private void makeToast(String s) 
  { 
      Context context = getApplicationContext();
      int duration = Toast.LENGTH_SHORT;
      Toast toast = Toast.makeText(context, s, duration);
      toast.show();
  }
  
  // user is taken to proper web site specified in sRegisterURI to register an account
  public void mRegisterUserAccount(View v)
  {
			 Uri uri = Uri.parse(sRegisterURI);
			 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
  }

  public void mLoginUserAccount(View v)
  {
	  String sTypedEmail = tvEmailForm.getText().toString();
	  String sTypedPassword = tvPasswordForm.getText().toString();
	  
	  
	  if (sTypedEmail.length() == 0 || sTypedPassword.length() == 0) 
	  {
	        // Password or Email is missing
	        makeToast("Please fill in both your username or password");
	    } 
	  else
	    {
		  //Perform Login with an AsyncTask, passing Email and Password as typed in forms by user
		  new LogInUserAsyncTask().execute(sTypedEmail, sTypedPassword);
		  //mLogInUserAccount(sTypedEmail, sTypedPassword);
		  

		  
	    }
	  	
	  	// Restore UI elements afterwards
		bLoginUserButton.setClickable(true);
		bRegisterUserButton.setClickable(true);
		
		bLoginUserButton.setVisibility(View.VISIBLE);
		bRegisterUserButton.setVisibility(View.VISIBLE);
		
		tvPasswordForm.setText("");

  }

	 private class LogInUserAsyncTask extends AsyncTask<String,Void,Integer>
	 {

		@Override
		protected Integer doInBackground(String... params) 
		{
			  String sTypedEmail = params[0];
			  String sTypedPassword = params[1];
			
				//Post Request used to get a token
				
				HttpClient myHTTP_Client = new DefaultHttpClient();
				HttpPost myHTTP_Post = new HttpPost(webServerURL_POST_InitiateLogin);
				
				try 
				{				
					JSONObject jsonObject_postRequest = new JSONObject();
					try 
					{
					    // add Email and Password to JSON object to send
						jsonObject_postRequest.put("email", sTypedEmail);
						jsonObject_postRequest.put("password", sTypedPassword);
					}
					catch (Exception e)
					{
						Log.e("jsonObject_postRequest() Error: ",Log.getStackTraceString(e));
					}
					
					myHTTP_Post.setEntity(new StringEntity(jsonObject_postRequest.toString()));
					//--------------- Set correct Header for POST request for JSON contents ------------------------------------------
					myHTTP_Post.setHeader("Content-type", "application/json");
					myHTTP_Post.setHeader("Accept", "application/json");
					//-----------------------------------------------------------------------------------------------
					
					//Pass HttpGet to httpClient, execute and listen for server response
					HttpResponse myResponse = myHTTP_Client.execute(myHTTP_Post);
					//Pass response to a statusLine variable
					StatusLine slResponse = myResponse.getStatusLine();
					//Returned statusCode of web server
					int statusCode = slResponse.getStatusCode();
					
					//StatusCode 200 => Successfully established connection with web server.
					if (statusCode == 200) 
					{
						// ------------- Parse response to String ----------------------------------
						BufferedReader rd = new BufferedReader(
						        new InputStreamReader(myResponse.getEntity().getContent()));
					 
						StringBuffer result = new StringBuffer();
						String line = "";
						while ((line = rd.readLine()) != null) 
						{
							result.append(line);
							
						}
						
						String responseString = result.toString();
						// --------------------------------------------------------------------------
						//makeToast("Response of POST request is:\n" + responseString);
						//makeToast("Response from web server received and parsed");
						
						//Convert responeString to JSON Object
						JSONObject jsonObject = new JSONObject(responseString);
						
						
						String sReturnedStatus = jsonObject.getString("status");
						
						if (sReturnedStatus.equals("success") == true)
						{
							
						String sReturnedToken = jsonObject.getString("access_token");
						int iReturnedAccountID = jsonObject.getInt("id");

						
						// ---------- On receiving successful response: Store token and ID in SharedPreferences of current user ----------------------------
						editorForUserAccountLoginActivity = spUserAccountLoginActivity.edit();
						editorForUserAccountLoginActivity.putInt(CURRENT_ACCOUNT_ID_STORAGE, iReturnedAccountID);
						editorForUserAccountLoginActivity.putString(CURRENT_ACCOUNT_TOKEN_STORAGE, sReturnedToken);
						editorForUserAccountLoginActivity.commit();
						// -----------------------------------------------------------------------------------------------------------------------------------
						
						  //Start new sleep session
						  new AsyncTaskNewSleepSession().execute();
						
						}
						else
						{
							return LOGIN_FAILED;
						}

					} 
					else //If statusCode is not 200: Server offline
					{
						return SERVER_OFFLINE;
					}
				} //End try
				catch (Exception e) 
					{
					Log.e("LoginActivity HTTTP Error: ",Log.getStackTraceString(e));
						return HTTP_ERROR;			
					}
			// Pass LOGIN_SUCCESSFUL to onPostExecute()
			return LOGIN_SUCCESS;
		}

		@Override
		protected void onPreExecute() 
		{
			super.onPreExecute();
			
			progressBarLoginActvity.setVisibility(View.VISIBLE);
			
			// Disable UI elements before AsyncTask is running
			bLoginUserButton.setClickable(false);
			bRegisterUserButton.setClickable(false);
			
			bLoginUserButton.setVisibility(View.INVISIBLE);
			bRegisterUserButton.setVisibility(View.INVISIBLE);
			
		}
		
		
		@Override
		protected void onProgressUpdate(Void... values) 
		{
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Integer result) 
		{
			super.onPostExecute(result);
			
			progressBarLoginActvity.setVisibility(View.INVISIBLE);
			
			
			if (result == LOGIN_SUCCESS)
			{
				Intent intent = new Intent(LoginActivity.this, SleepStreamActivity.class);
				startActivity(intent);
			}
			else if (result == SERVER_OFFLINE)
			{
				Toast.makeText(LoginActivity.this,"Error: Web Server not responding", Toast.LENGTH_LONG).show();
			}
			else if (result == LOGIN_FAILED)
			{
				Toast.makeText(LoginActivity.this,"Username or password wrong. Please retry or register an account.", Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(LoginActivity.this,"HTTP ERROR", Toast.LENGTH_LONG).show();
			}
			
		}
		

		}
	 
	private class AsyncTaskNewSleepSession extends AsyncTask<Void,Void,String>
		{
				@Override
				protected String doInBackground(Void... params) 
				{
					String sReturnedSleepID = null;
					HttpClient myHTTP_Client = new DefaultHttpClient();
					HttpPost myHTTP_Post = new HttpPost(webServerURL_POST_InitiateSleep);
				
					try 
					{
						
						JSONObject jsonObject_postRequest = new JSONObject();
						try 
						{
						    // adding some keys/pair values in JSON object to send. value of "content" is sent to server along with sleep id request.
							jsonObject_postRequest.put("content", ",");
						}
						catch (Exception e)
						{
							Log.e("jsonObject_postRequest.put() Error: ",Log.getStackTraceString(e));
						}
						
						myHTTP_Post.setEntity(new StringEntity(jsonObject_postRequest.toString()));
						//--------------- Set correct Header for POST request for JSON contents ------------------------------------------
						myHTTP_Post.setHeader("Content-type", "application/json");
						myHTTP_Post.setHeader("Accept", "application/json");
						myHTTP_Post.setHeader("Authorization" , URL_TOKEN_PREFIX + spUserAccountLoginActivity.getString(CURRENT_ACCOUNT_TOKEN_STORAGE, "ERROR-COULD-NOT-FIND-TOKEN"));
						//-----------------------------------------------------------------------------------------------
						
						//Pass HttpPost to httpClient, execute and listen for server response
						HttpResponse myResponse = myHTTP_Client.execute(myHTTP_Post);
						//Pass response to a statusLine variable
						StatusLine slResponse = myResponse.getStatusLine();
						//Returned statusCode of web server
						int statusCode = slResponse.getStatusCode();
						
						//StatusCode 200 => Success
						if (statusCode == 200) 
						{
							// ------------- Parse response to String ----------------------------------
							BufferedReader rd = new BufferedReader(
							        new InputStreamReader(myResponse.getEntity().getContent()));
						 
							StringBuffer result = new StringBuffer();
							String line = "";
							while ((line = rd.readLine()) != null) 
							{
								result.append(line);
							}
							
							String responseString = result.toString();
							// --------------------------------------------------------------------------
							
							//Convert responeString to JSON Object
							JSONObject jsonObject = new JSONObject(responseString);
							
							// WEb Server return ID of new sleep session. This is ID stored in sReturnedSleepID
							sReturnedSleepID = jsonObject.getString("sleep_id");
							
							// ---------- Store response info in SharedPreferences ----------------------------
							editorForUserAccountLoginActivity2 = spUserAccountLoginActivity.edit();
							editorForUserAccountLoginActivity2.putString(CURRENT_ACCOUNT_LATEST_SLEEP_ID_STORAGE, sReturnedSleepID);
							editorForUserAccountLoginActivity2.commit();
							// -----------------------------------------------------------------------------
						}
						else
						{
							return "Server offline!";
						}
					}
					
					catch (Exception e) 
					{
						Log.e("AsyncTaskNewSleepSession Error: ",Log.getStackTraceString(e));
						return "HTTP_ERROR!";
					}
					
					return sReturnedSleepID;
				}

				@Override
				protected void onPostExecute(String result) 
				{
					super.onPreExecute();
					
					if(result.equals("Server offline!"))
					{
						makeToast(result);
					}
					else if(result.equals("HTTP_ERROR!"))
					{
						makeToast(result);
					}
					else
					{
						makeToast("New sleep session started with ID: " + result);
					}
				}
				
				
				
				
	 
		}

}