package com.MyTestApp.pebbletest;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.R.id;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class WebServerActivity extends Activity 
{
	
	private final String SAVED_USER_ACCOUNT_INFO = "SharedPreferences_UserAccount";
	private final String CURRENT_ACCOUNT_ID_STORAGE = "userAccountIdStorage";
	private final String CURRENT_ACCOUNT_TOKEN_STORAGE = "TokenStorage";
	private final String CURRENT_ACCOUNT_LATEST_SLEEP_ID_STORAGE = "latestSleepIDRequested";
	private final int ERROR_STRING = 999999999;
	private final String sWebsiteURI = "https://warm-journey-4594.herokuapp.com/";
	
	private final String SLEEP_SESSION_ID_ERROR = "No existing sleep session ID found";
	
	SharedPreferences spUserAccount; 
	SharedPreferences.Editor editorForUserAccount;
	SharedPreferences.Editor editorForUserAccountNo2;
	TextView tvParsedJSONResponse;
	TextView tvUserAccountInfoDisplayer;
	ProgressBar pgWebServerActivityProgressBar;
	
	//Web server info
	final String webServerURL_GET = "https://warm-journey-4594.herokuapp.com/api/v1/users/";
	final String webServerURL_POST_InitiateLogin = "https://warm-journey-4594.herokuapp.com/api/v1/sessions";
	final String webServerURL_POST_InitiateSleep = "https://warm-journey-4594.herokuapp.com/api/v1/sleeps";
	final String webServerURL_PUT = "https://warm-journey-4594.herokuapp.com/api/v1/sleeps/";
	
	final String URL_TOKEN_PREFIX = "Token ";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webserver);
		
		tvParsedJSONResponse = (TextView) findViewById(R.id.tvWebServerResponse);
		tvUserAccountInfoDisplayer = (TextView) findViewById(R.id.tvWebServerAccountDisplayer);
		spUserAccount = getSharedPreferences(SAVED_USER_ACCOUNT_INFO, MODE_PRIVATE);
		pgWebServerActivityProgressBar = (ProgressBar) findViewById(R.id.pbWebServerProgressBar);
		
		/* Do not update upper text section
		tvUserAccountInfoDisplayer.setText
		(
				"STORED TOKEN & USER ID:\n\n"
			+	"User ID: " + spUserAccount.getInt(CURRENT_ACCOUNT_ID_STORAGE, ERROR_STRING) + "		(Default value = 999999999)\n"
			+	"Token: "	+ spUserAccount.getString(CURRENT_ACCOUNT_TOKEN_STORAGE, "No existing token found")

		);
		*/
		
		tvParsedJSONResponse.setText("Current sleep session ID is: " + (spUserAccount.getString(CURRENT_ACCOUNT_LATEST_SLEEP_ID_STORAGE, SLEEP_SESSION_ID_ERROR).toString()));
		
		/*
		// Android StrictMode (on by default), this blocks UI while background operations are ongoing - Comment out to turn on, Uncomment to turn off -------------
		StrictMode.ThreadPolicy policy = new StrictMode.
				ThreadPolicy.Builder().permitAll().build();
				StrictMode.setThreadPolicy(policy); 
		// ------------------------------------------------------------------------------------------------
		*/
		
	}
	
	// ---------------------------------------------------- not in use -----------------------------------------------------------
	
	//Post Request used to get a token
	public void mPostRequestWebServer(View v)
	{
		//makeToast("button works");
		
		
		HttpClient myHTTP_Client = new DefaultHttpClient();
		HttpPost myHTTP_Post = new HttpPost(webServerURL_POST_InitiateLogin);
		
		try 
		{
			
			/* ================= Used when submitting form fields instead of JSON =======================
			List<NameValuePair> postRequestContents = new ArrayList<NameValuePair>();
			postRequestContents.add(new BasicNameValuePair("This is a Post Request", "I am King Cobra!"));
			postRequestContents.add(new BasicNameValuePair("Save tha Queen!", "Queen Latifa!"));
			postRequestContents.add(new BasicNameValuePair("UserName", "Einsteiner"));
			postRequestContents.add(new BasicNameValuePair("Password", "ImRichDawg"));
			postRequestContents.add(new BasicNameValuePair("Email", "rikki_lakey@gmail.com"));
			postRequestContents.add(new BasicNameValuePair("AuthToken", "315783750839023"));
			myHTTP_Post.setEntity(new UrlEncodedFormEntity(postRequestContents));
			========================================================================================== */
			
			
			JSONObject jsonObject_postRequest = new JSONObject();
			try 
			{
			    // adding some keys/pair values in JSON object to send
				jsonObject_postRequest.put("email", "presidentnixon@blabla.com");
				jsonObject_postRequest.put("password", "obamaobama");
			}
			catch (Exception e)
			{
				makeToast("Problem putting info in JSON Object");
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
				//makeToast("Response of POST request is:\n" + responseString);
				makeToast("Response from web server received and parsed");
				
				//Convert responeString to JSON Object
				JSONObject jsonObject = new JSONObject(responseString);
				
				String sReturnedStatus = jsonObject.getString("status");
				
				if (sReturnedStatus.equals("success") == true)
				{
				String sReturnedToken = jsonObject.getString("access_token");
				int iReturnedAccountID = jsonObject.getInt("id");
				
				
				// ---------- Store response info in SharedPreferences ----------------------------
				editorForUserAccount = spUserAccount.edit();
				editorForUserAccount.putInt(CURRENT_ACCOUNT_ID_STORAGE, iReturnedAccountID);
				editorForUserAccount.putString("ResponseStatusStorage", sReturnedStatus);
				editorForUserAccount.putString(CURRENT_ACCOUNT_TOKEN_STORAGE, sReturnedToken);
				editorForUserAccount.commit();
				// -----------------------------------------------------------------------------
				
				//Grab info from SharedPreferences and show what is stored in a TextView
				tvUserAccountInfoDisplayer.setText
				(
						"STORED TOKEN & USER ID:\n\n"
					+	"User ID: " + spUserAccount.getInt(CURRENT_ACCOUNT_ID_STORAGE, ERROR_STRING) + "		(Default value = 999999999)\n"
					+	"Token: "	+ spUserAccount.getString(CURRENT_ACCOUNT_TOKEN_STORAGE, "No existing token found")

				);
				
				tvParsedJSONResponse.setText(jsonObject.toString(2));

				/*
				
				//---print out the content of the json feed---
				for (int i = 0; i < jsonArray.length(); i++) 
				{
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					}
				
				 */
				}
				else
				{
					makeToast("Username or password wrong. Please Retry");
				}
			} 
			else //If statusCode is not 200
			{
				makeToast("No web server response: Server might be offline");
			}
			} 
		catch (Exception e) 
			{
				makeToast("HTTP Error");
				Log.e("Error: ",Log.getStackTraceString(e));
			}
	}
	
	// ---------------------------------------------------- not in use -----------------------------------------------------------
	
	
	
	
	public void mPostRequestWebServerStartSleep(View v)
	{
			
			//makeToast("button works");
			
			// If token or user ID cannot be found in SharedPreferences, do not perform POST request to web server
			if((spUserAccount.contains(CURRENT_ACCOUNT_ID_STORAGE) && spUserAccount.contains(CURRENT_ACCOUNT_TOKEN_STORAGE) == false))
			{
				makeToast("Could not retrieve token session or user ID");
				return;
			}
			
			new AsyncTaskNewSleepSessionTwo().execute();
		
	}
	
	
	public void mGetRequestWebServer(View v)
	{
		// If token or user ID cannot be found in SharedPreferences, do not perform GET request to web server
		if((spUserAccount.contains(CURRENT_ACCOUNT_ID_STORAGE) && spUserAccount.contains(CURRENT_ACCOUNT_TOKEN_STORAGE) == false))
		{
			makeToast("Could not retrieve token session or user ID");
			return;
		}
		
		new AsyncTaskGETRequestWebServer().execute();
	}
	
	// ------------------------ Disabled in this activity ----------------------------------------------------------------------------------------
	public void mPutRequestWebServer(View v)
	{
		//makeToast("button works");
		
		// If token, user ID or sleep ID cannot be found in SharedPreferences, do not perform PUT request to web server
		if((spUserAccount.contains(CURRENT_ACCOUNT_ID_STORAGE) && spUserAccount.contains(CURRENT_ACCOUNT_TOKEN_STORAGE) && spUserAccount.contains(CURRENT_ACCOUNT_LATEST_SLEEP_ID_STORAGE) == false))
		{
			makeToast("Could not retrieve token, user ID, or latest sleep ID");
			return;
		}
		
		//int sleepIDToConvert = spUserAccount.getString(CURRENT_ACCOUNT_LATEST_SLEEP_ID_STORAGE, ERROR_STRING);
		//String sSleepIDConverted = Integer.toString(sleepIDToConvert);
		String webServerURLAppendedWithSleepID = webServerURL_PUT + spUserAccount.getString(CURRENT_ACCOUNT_LATEST_SLEEP_ID_STORAGE, "Could not find sleep ID");
		
		
		HttpClient myHTTP_Client = new DefaultHttpClient();
		HttpPut myHTTP_Put = new HttpPut(webServerURLAppendedWithSleepID);
		
		try {
			/* ---------- info sent as FORM ENTITY --------------------------
			List<NameValuePair> putRequestContents = new ArrayList<NameValuePair>();
			putRequestContents.add(new BasicNameValuePair("This is a PUT Request!", "PEW PEW!"));
			putRequestContents.add(new BasicNameValuePair("UserName", "ThanksObama"));
			putRequestContents.add(new BasicNameValuePair("Password", "CookieMonster666"));
			putRequestContents.add(new BasicNameValuePair("Email", "corgi4eva@beibermail.com"));
			putRequestContents.add(new BasicNameValuePair("AuthToken", "3985723857302"));
			myHTTP_Put.setEntity(new UrlEncodedFormEntity(putRequestContents));
			----------------------------------------------------------------- */
			
			
			
			JSONObject jsonObject_postRequest = new JSONObject();
			try 
			{
			    // adding some keys/pair values in JSON object to send. value of "content" is sent to server along with sleep id request.
				jsonObject_postRequest.put("content", "2,5,4,2,3,6,8,4,6,4,5,2,3,1,3,6,4,7,8");
			}
			catch (Exception e)
			{
				makeToast("Problem putting sleep data in JSON Object");
			}
		

			myHTTP_Put.setEntity(new StringEntity(jsonObject_postRequest.toString()));
			//--------------- Set correct Header for POST request for JSON contents ------------------------------------------
			myHTTP_Put.setHeader("Content-type", "application/json");
			myHTTP_Put.setHeader("Accept", "application/json");
			myHTTP_Put.setHeader("Authorization" , URL_TOKEN_PREFIX + spUserAccount.getString(CURRENT_ACCOUNT_TOKEN_STORAGE, "ERROR-COULD-NOT-FIND-TOKEN"));
			//-----------------------------------------------------------------------------------------------
			

			
			
			//Pass HttpGet to httpClient, execute and listen for server response
			HttpResponse myResponse = myHTTP_Client.execute(myHTTP_Put);
			//Pass response to a statusLine variable
			StatusLine slResponse = myResponse.getStatusLine();
			//Returned statusCode of web server
			int statusCode = slResponse.getStatusCode();
			
			//StatusCode 200 => Success
			if (statusCode == 200) 
			{
			// On success, pass response to an entity variable
				//makeToast("Post Request sent to server with address:\n" + webURL);
				
				BufferedReader rd = new BufferedReader(
				        new InputStreamReader(myResponse.getEntity().getContent()));
			 
				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) 
				{
					result.append(line);
					
				}
				
				String responseString = result.toString();
				makeToast("Response of POST request is:\n" + responseString);
				
				//JSON
				JSONObject jsonObject = new JSONObject(responseString);
				tvParsedJSONResponse.setText(jsonObject.toString(2));	
			} 
			else //If statusCode is not 200
			{
				makeToast("Server respone error: Did not return 200");
			}
			} 
		catch (Exception e) 
			{
				makeToast("HTTP Error");
				Log.e("mPutRequestWebServer() Error: ",Log.getStackTraceString(e));
			}
	}
	// ------------------------ Disabled in this activity ------------------------------------------------------------------------------------------------------------------------------
	
	public void mVisitWebSite(View v)
	{
		 Uri uri = Uri.parse(sWebsiteURI);
		 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}
	
	//Toastmaker
  private void makeToast(String s) 
  {
      Context context = getApplicationContext();
      int duration = Toast.LENGTH_SHORT;
      Toast toast = Toast.makeText(context, s, duration);
      toast.show();
  }
  
  
private class AsyncTaskNewSleepSessionTwo extends AsyncTask<Void,Void,String>
	{		
			
		
			@Override
	protected void onPreExecute() 
			{
				super.onPreExecute();
		
				pgWebServerActivityProgressBar.setVisibility(View.VISIBLE);
		
			}

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
					myHTTP_Post.setHeader("Authorization" , URL_TOKEN_PREFIX + spUserAccount.getString(CURRENT_ACCOUNT_TOKEN_STORAGE, "ERROR-COULD-NOT-FIND-TOKEN"));
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
						editorForUserAccountNo2 = spUserAccount.edit();
						editorForUserAccountNo2.putString(CURRENT_ACCOUNT_LATEST_SLEEP_ID_STORAGE, sReturnedSleepID);
						editorForUserAccountNo2.commit();
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
				
				pgWebServerActivityProgressBar.setVisibility(View.INVISIBLE);
				
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
					tvParsedJSONResponse.setText("Current sleep session ID is: " + result);
					makeToast("Current sleep session finished. New sleep session started with ID: " + result);
				}
				

			}
			
	
	}
  
private class AsyncTaskGETRequestWebServer extends AsyncTask<Void,Void,String>
{

	@Override
	protected String doInBackground(Void... params) 
	{	
		String sIDResultString;
		//Attach user ID to the GET URL
		String webServerURLAppendedWithUserID = webServerURL_GET + spUserAccount.getInt(CURRENT_ACCOUNT_ID_STORAGE, ERROR_STRING);
		//makeToast(webServerURLAppendedWithID);
		HttpClient myHTTP_Client = new DefaultHttpClient();

		HttpGet myHTTP_Get = new HttpGet(webServerURLAppendedWithUserID);
		
		try {
			
			// Prepare GET request with token in header
			myHTTP_Get.setHeader("Authorization" , URL_TOKEN_PREFIX + spUserAccount.getString(CURRENT_ACCOUNT_TOKEN_STORAGE, "ERROR-COULD-NOT-FIND-TOKEN"));

			
			//Pass HttpGet to httpClient, execute and listen for server response
			HttpResponse myResponse = myHTTP_Client.execute(myHTTP_Get);
			//Pass response to a statusLine variable
			StatusLine slResponse = myResponse.getStatusLine();
			//Returned statusCode of web server
			int statusCode = slResponse.getStatusCode();
			//StatusCode 200 => Success
			if (statusCode == 200) 
			{
				// ------------- Parse web server response to a string ------------------
				BufferedReader rd = new BufferedReader(
				        new InputStreamReader(myResponse.getEntity().getContent()));
			 
				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) 
				{
					result.append(line);
					
				}
				
				String responseString = result.toString();
				// ----------------------------------------------------------------------
				
				//makeToast("Response of GET request is:\n" + responseString);
				//makeToast("Response from web server received and parsed");
				
				
				//JSON
				JSONObject jsonObject = new JSONObject(responseString);
				
				JSONArray jsonSleepSessionsArray = jsonObject.getJSONArray("sleeps");
				
				int iSleepSessionArrayLength = jsonSleepSessionsArray.length();
				
				int iSleepSessionID;
				//String sleepSessionData = null;
				//String sleepSessionCreationDate = null;
				//String sleepSessionLastUpdate = null;
				
				
				JSONObject jsonArrayIndexItemStorage = null;
				
				StringBuilder sbAllSLeepSessionIDs = new StringBuilder("\n" + "ID:s of previously recorded sleep sessions:" + "\n");
				
				//StringBuilder sbAllSleepSessionCreationDates = null;
				//StringBuilder sbAllSleepSessionsLastUpdateDates = null;
				//StringBuilder sbAllSleepSessionsData = null;
				
				
				
				for(int i = 0 ; (i < (iSleepSessionArrayLength - 1)) ; i++)
				{
					jsonArrayIndexItemStorage = jsonSleepSessionsArray.getJSONObject(i);
					
					iSleepSessionID = jsonArrayIndexItemStorage.getInt("id");
					sbAllSLeepSessionIDs.append(iSleepSessionID + ", ");
					
				}
				
				//removes last 2 characters, which are ", "
				sbAllSLeepSessionIDs.setLength(sbAllSLeepSessionIDs.length() - 2);
				
				sbAllSLeepSessionIDs.append("\n");
				sIDResultString = sbAllSLeepSessionIDs.toString();
				

				
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
		
		return sIDResultString;
	}

	@Override
	protected void onPreExecute() 
	{
		super.onPreExecute();
		
		pgWebServerActivityProgressBar.setVisibility(View.VISIBLE);
		
	}

	@Override
	protected void onPostExecute(String result) 
	{
		super.onPostExecute(result);
		
		
		
		pgWebServerActivityProgressBar.setVisibility(View.INVISIBLE);
		
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
			makeToast("Updated. All sleep sessions found displayed below.");
			//tvParsedJSONResponse.setText(jsonObject.toString(2));
			tvParsedJSONResponse.setText
			(
					"Current sleep session ID is: " 
				+ spUserAccount.getString(CURRENT_ACCOUNT_LATEST_SLEEP_ID_STORAGE, "No existing sleep session ID found") 
				+ "\n"
				+ result
				+ "\n"
				/* ----- This turns on display of detailed information of web server response -------------
				+ "Response string is:\n"
				+ jsonObject.toString(2)	
				+ "\n"
				-----------------------------------------------------------------------------------------*/
			);
		}
		

		
	}
	
	
	
	
}
  
}