package com.MyTestApp.pebbletest;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class AudiolistActivityInternalStorage extends ListActivity 
{
	
  private Cursor cursor;
  Uri uriInternalSDCard = null;
  SharedPreferences spMusicSettings;
  SharedPreferences.Editor editor;
  String sMusicName;
  String sMusicPath;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.listview_audiolist);
    
    //String queryFilter = MediaStore.Audio.Media.IS_MUSIC + " != 0";
    
    try
    {
    uriInternalSDCard = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
    }
    catch(Exception e)
    {
    	makeToast("Error: Cannot find Internal storage on your device");
    	return;
    }
    
    //Column to retrieve from MediaStore
    String[] sa_projection = 
    	{ 
    		android.provider.MediaStore.Audio.Media._ID, 
    		android.provider.MediaStore.Audio.Media.DISPLAY_NAME,
    		android.provider.MediaStore.Audio.Media.TITLE,
    		android.provider.MediaStore.Audio.Media.IS_MUSIC,
    		android.provider.MediaStore.Audio.Media.DATA,
    	};
    
    //Result of MediaSstore query
    cursor = getContentResolver().query(uriInternalSDCard, sa_projection, null, null, null);

    //Display selected results in selected listview
    String[] displayFields = new String[] { MediaStore.Audio.Media.DISPLAY_NAME };
    int[] displayViews = new int[] { android.R.id.text1 };
    setListAdapter(new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, displayFields, displayViews, 0));
  }
  
  //When item in music list is clicked, save it in sharedpreferences and close activity
  protected void onListItemClick(ListView l, View v, int position, long id) 
  {

	  cursor.moveToPosition(position);
	  sMusicName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
	  sMusicPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
	  
	  //Store chosen music in sharedpreferences with key "MusicChoiceKey"
	  spMusicSettings = getSharedPreferences("SharedPreferencesMusic", Context.MODE_PRIVATE);
	  editor = spMusicSettings.edit();
		editor.putString("MusicName", sMusicName);
		editor.putString("MusicPath", sMusicPath);
		editor.commit();
		//makeToast("The selected music is:\n" + musicName);
		makeToast("Music updated");
		
		cursor.close();
		setResult(RESULT_OK);
		this.finish();
   
  }
  
	//Toastmaker
  private void makeToast(String s) 
  {
	  
      Context context = getApplicationContext();
      int duration = Toast.LENGTH_SHORT;
      Toast toast = Toast.makeText(context, s, duration);
      toast.show();
  }
  
  
}