package net.arist.sunplay;

import static android.os.Build.VERSION.SDK_INT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.os.Process;
import android.os.Environment;
import android.text.*;

import nightradio.sunvoxlib.SunVoxLib;
import android.widget.*;
import android.view.View.*;
import android.content.res.*;
import android.content.Context;

public class MainActivity extends Activity {

	int sunvox_version = 0;
	int fileoffset = 0;
	int svcount = 0;
	String svfiles[];
	String svsong = "";
	String baseDir;
	int song_index = 0;
	TextView song_title;
	TextView selecter;
	Dialog dialog;
	Context context;
	String cfg = "";
	int newfreq;
	int newbuffer;
	int sample_rate = 44100;
	/**
	 * permissions request code
	 */
	private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

	/**
	 * Permissions that need to be explicitly requested from end user.
	 */
	private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };

    
	@Override
	protected void onCreate( Bundle savedInstanceState ) 
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		SetSustainedPerformanceMode( true );
		int optimal_buffer_size = GetAudioOutputBufferSize();
		int optimal_sample_rate = GetAudioOutputSampleRate();
		int sample_rate = 44100;
		if( optimal_sample_rate > 0 && optimal_buffer_size > 0 )
		{
			sample_rate = optimal_sample_rate;
			cfg += "buffer=" + optimal_buffer_size;
		}
		int[] cores = GetExclusiveCores();
		if( cores != null )
		{
			if( cores.length > 0 )
			{
				cfg += "|exclcores=";
				for( int i = 0; i < cores.length; i++ )
				{
					if( i > 0 ) cfg += ",";
					cfg += cores[i];
				}
			}
		}
		Log.v( "CFG", cfg );
		if (SDK_INT >= Build.VERSION_CODES.R) {
			ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);
			if (!Environment.isExternalStorageManager()){
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
				Uri uri = Uri.fromParts("package", this.getPackageName(), null);
				intent.setData(uri);
				startActivity(intent);
			}
		}

		song_title = new TextView(this);
		song_title.findViewById(R.id.songtitle);
		context = getApplicationContext();
		baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		Log.v( "PATH", baseDir );
        ArrayList<String> result = new ArrayList<String>(); //ArrayList cause you don't know how many files there is
        File folder = new File(baseDir + "/sunvoxfiles"); //This is just to cast to a File type since you pass it as a String
        File[] filesInFolder = folder.listFiles(); // This returns all the folders and files in your path
        for (File file : filesInFolder) { //For each of the entries do:
            if (!file.isDirectory() && getFileExtension(file).equals("sunvox")) { //check that it's not a dir and is a *.sunvox file
                result.add(new String(file.getName())); //push the filename as a string
            }
        }
        svfiles = result.toArray(new String[0]);
        Arrays.sort(svfiles);
		((TextView) findViewById(R.id.songtitle)).setText(svfiles[0]);
		
		selecter = findViewById(R.id.selecter);
		selecter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize dialog
                dialog=new Dialog(MainActivity.this);
 
                // set custom dialog
                dialog.setContentView(R.layout.dialog_searchable_spinner);
 
                // set custom height and width
                dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 
                // set transparent background
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
 
                // show dialog
                dialog.show();
 
                // Initialize and assign variable
                EditText editText=dialog.findViewById(R.id.edit_text);
                ListView listView=dialog.findViewById(R.id.list_view);
 
                // Initialize array adapter
                final ArrayAdapter<String> adapter=new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1,Arrays.asList(svfiles));
 
                // set adapter
                listView.setAdapter(adapter);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
 
                    }
                    
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 
                    }
 
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        adapter.getFilter().filter(s);
                    }
                });
 
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // when item selected from list
                        // set selected item on textView
						svsong = svfiles[position];
                        ((TextView) findViewById(R.id.songtitle)).setText(adapter.getItem(position));
						
						if( sunvox_version > 0 )
						{
							SunVoxLib.stop( 0 );
						}
						
						int rv = SunVoxLib.load( 0, baseDir + "/sunvoxfiles/" + svsong );
						if( rv == 0 )
							Log.i( "SunVoxPlayer", "Song loaded" );
						else
							Log.e( "SunVoxPlayer", "Song load error " + rv + " " + baseDir + "/sunvoxfiles/" + svsong );        
						
						if(((EditText)findViewById(R.id.Offset)).getText().toString().matches("")){
							fileoffset = 0;
						}else{
							fileoffset = Integer.parseInt(((EditText)findViewById(R.id.Offset)).getText().toString());
						}
						if( sunvox_version > 0 )
						{
							//Disable autostop:
							SunVoxLib.set_autostop( 0, 0 );
				
							//Rewind (go to the offset):
							SunVoxLib.rewind( 0, fileoffset );
				
							//Set volume:
							SunVoxLib.volume( 0, 256 );
							SunVoxLib.play( 0 );	        
						}
						
                        // Dismiss dialog
                        dialog.dismiss();
                    }
                });
            }
        });

		sunvox_version = SunVoxLib.init( cfg, sample_rate, 2, 0 );
		if( sunvox_version > 0 )
	    {
	        int major = ( sunvox_version >> 16 ) & 255;
	        int minor1 = ( sunvox_version >> 8 ) & 255;
	        int minor2 = ( sunvox_version ) & 255;
	        Log.i( "SunVoxPlayer", "SunVox lib version: " + major + " " + minor1 + " " + minor2 );
	        
	        //Open audio slot #0:
	        SunVoxLib.open_slot( 0 );
	        
			int rv = SunVoxLib.load( 0, baseDir + "/sunvoxfiles/" + svfiles[0] );
	        if( rv == 0 )
	        	Log.i( "SunVoxPlayer", "Song loaded" );
	        else
	        	Log.e( "SunVoxPlayer", "Song load error " + rv + " " + baseDir + "/sunvoxfiles/" + svsong );
	    }
		else
		{
			Log.e( "SunVoxPlayer", "Can't open SunVox library" );
		}
	}

	@Override
	protected void onDestroy ()
	{
		if( sunvox_version > 0 )
		{
	        SunVoxLib.close_slot( 0 );
			SunVoxLib.deinit();
			Log.i( "SunVoxPlayer", "SunVox engine closed" );
		}
		super.onDestroy();
	}
	
	public void playButtonClick( View view )
	{
		if(((EditText)findViewById(R.id.Offset)).getText().toString().matches("")){
			fileoffset = 0;
		}else{
			fileoffset = Integer.parseInt(((EditText)findViewById(R.id.Offset)).getText().toString());
		}
		//PLAY:
		if( sunvox_version > 0 )
		{
	        //Disable autostop:
	        SunVoxLib.set_autostop( 0, 0 );
	        
	        //Rewind (go to the line #0):
	        SunVoxLib.rewind( 0, fileoffset );
	        
	        //Set volume:
	        SunVoxLib.volume( 0, 256 );

	        //Play from offset:
	        SunVoxLib.play( 0 );	        
		}
	}

	public void stopButtonClick( View view )
	{
		//STOP:
		if( sunvox_version > 0 )
		{
			SunVoxLib.stop( 0 );
		}
	}
	
	@SuppressLint("SuspiciousIndentation")
    public void PrevSong(View view){
		if(song_index <= 0){
			song_index = svfiles.length - 1;
		}else{
			song_index--;
		}
		svsong = svfiles[song_index];
		((TextView) findViewById(R.id.songtitle)).setText(svfiles[song_index]);
		
		if( sunvox_version > 0 )
		{
			SunVoxLib.stop( 0 );
		}
		
        int rv = SunVoxLib.load( 0, baseDir + "/sunvoxfiles/" + svsong );
		if( rv == 0 )
			Log.i( "SunVoxPlayer", "Song loaded" );
		else
			Log.e( "SunVoxPlayer", "Song load error " + rv + " " + baseDir + "/sunvoxfiles/" + svsong );
        
        if(((EditText)findViewById(R.id.Offset)).getText().toString().matches("")){
			fileoffset = 0;
		}else{
			fileoffset = Integer.parseInt(((EditText)findViewById(R.id.Offset)).getText().toString());
		}
		if( sunvox_version > 0 )
		{
	        //Disable autostop:
	        SunVoxLib.set_autostop( 0, 0 );

	        //Rewind (go to the offset):
	        SunVoxLib.rewind( 0, fileoffset );

	        //Set volume:
	        SunVoxLib.volume( 0, 256 );
	        SunVoxLib.play( 0 );	        
		}
	}
	
	public void NextSong(View view){
		if(song_index >= svfiles.length - 1){
			song_index = 0;
		}else{
			song_index++;
		}
		svsong = svfiles[song_index];
		((TextView) findViewById(R.id.songtitle)).setText(svfiles[song_index]);

		if( sunvox_version > 0 )
		{
			SunVoxLib.stop( 0 );
		}
		
		int rv = SunVoxLib.load( 0, baseDir + "/sunvoxfiles/" + svsong );
		if( rv == 0 )
			Log.i( "SunVoxPlayer", "Song loaded" );
		else
			Log.e( "SunVoxPlayer", "Song load error " + rv + " " + baseDir + "/sunvoxfiles/" + svsong );        
        
        if(((EditText)findViewById(R.id.Offset)).getText().toString().matches("")){
			fileoffset = 0;
		}else{
			fileoffset = Integer.parseInt(((EditText)findViewById(R.id.Offset)).getText().toString());
		}
		if( sunvox_version > 0 )
		{
	        //Disable autostop:
	        SunVoxLib.set_autostop( 0, 0 );

	        //Rewind (go to the offset):
	        SunVoxLib.rewind( 0, fileoffset );

	        //Set volume:
	        SunVoxLib.volume( 0, 256 );
	        SunVoxLib.play( 0 );	        
		}
	}
	
	public void SetConfig(View view){
		
        if( sunvox_version > 0 )
		{
			SunVoxLib.stop( 0 );
		    SunVoxLib.close_slot( 0 );
			SunVoxLib.deinit();
			Log.i( "SunVoxPlayer", "SunVox engine closed" );
		}
        
        newfreq = Integer.parseInt(((EditText)findViewById(R.id.freqrateedit)).getText().toString());
        newbuffer = Integer.parseInt(((EditText)findViewById(R.id.bufferedit)).getText().toString());
        cfg = new String("");
        
        sample_rate = newfreq;
		cfg += "buffer=" + newbuffer;
		
        int[] cores = GetExclusiveCores();
		if( cores != null )
		{
			if( cores.length > 0 )
			{
				cfg += "|exclcores=";
				for( int i = 0; i < cores.length; i++ )
				{
					if( i > 0 ) cfg += ",";
					cfg += cores[i];
				}
			}
		}
		Log.v( "CFG", cfg );
        
        sunvox_version = SunVoxLib.init( cfg, sample_rate, 2, 0 );
		if( sunvox_version > 0 )
	    {
	        int major = ( sunvox_version >> 16 ) & 255;
	        int minor1 = ( sunvox_version >> 8 ) & 255;
	        int minor2 = ( sunvox_version ) & 255;
	        Log.i( "SunVoxPlayer", "SunVox lib version: " + major + " " + minor1 + " " + minor2 );
	        
	        //Open audio slot #0:
	        SunVoxLib.open_slot( 0 );
	        
            int rv = SunVoxLib.load( 0, baseDir + "/sunvoxfiles/" + svsong );
	        if( rv == 0 )
	        	Log.i( "SunVoxPlayer", "Song loaded" );
	        else
	        	Log.e( "SunVoxPlayer", "Song load error " + rv + " " + baseDir + "/sunvoxfiles/" + svsong );
	    }
		else
		{
			Log.e( "SunVoxPlayer", "Can't open SunVox library" );
		}
        
        if(((EditText)findViewById(R.id.Offset)).getText().toString().matches("")){
			fileoffset = 0;
		}else{
			fileoffset = Integer.parseInt(((EditText)findViewById(R.id.Offset)).getText().toString());
		}
		if( sunvox_version > 0 )
		{
	        //Disable autostop:
	        SunVoxLib.set_autostop( 0, 0 );

	        //Rewind (go to the offset):
	        SunVoxLib.rewind( 0, fileoffset );

	        //Set volume:
	        SunVoxLib.volume( 0, 256 );
	        SunVoxLib.play( 0 );	        
		}
	}
		
    private byte[] convertStreamToByteArray( InputStream is ) throws IOException 
	{
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    byte[] buff = new byte[ 10240 ];
	    int i = Integer.MAX_VALUE;
	    while( ( i = is.read( buff, 0, buff.length ) ) > 0 ) 
	    {
	        baos.write( buff, 0, i );
	    }
	    return baos.toByteArray(); // be sure to close InputStream in calling function
	}

	//Get the number of audio frames that the HAL (Hardware Abstraction Layer) buffer can hold.
	//You should construct your audio buffers so that they contain an exact multiple of this number.
	//If you use the correct number of audio frames, your callbacks occur at regular intervals, which reduces jitter.
	private int GetAudioOutputBufferSize()
	{
		if( android.os.Build.VERSION.SDK_INT < 17 ) return 0; // < 4.2
		Context ctx = getApplicationContext();
		AudioManager am = (AudioManager) ctx.getSystemService(ctx.AUDIO_SERVICE);
		String frames = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
		return Integer.parseInt(frames);
	}

	private int GetAudioOutputSampleRate()
	{
		if( android.os.Build.VERSION.SDK_INT < 17 ) return 0; // < 4.2
		Context ctx = getApplicationContext();
		AudioManager am = (AudioManager) ctx.getSystemService(ctx.AUDIO_SERVICE);
		String rate = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
		return Integer.parseInt(rate);
	}

	//On some devices, the foreground process may have one or more CPU cores exclusively reserved for it.
	//This method can be used to retrieve which cores that are (if any),
	//so the calling process can then use sched_setaffinity() to lock a thread to these cores.
	private int[] GetExclusiveCores()
	{
		if( android.os.Build.VERSION.SDK_INT < 24 ) return null; // < 7.0
		int[] rv = null;
		try {
			rv = android.os.Process.getExclusiveCores();
		} catch( RuntimeException e ) {
			Log.w( "GetExclusiveCores", "getExclusiveCores() is not supported on this device");
		}
		return rv;
	}

	//Sustained performance mode is intended to provide a consistent level of performance for a prolonged amount of time
	private int SetSustainedPerformanceMode( boolean enable )
	{
		if( android.os.Build.VERSION.SDK_INT < 24 ) return -1; // < 7.0
		if( getWindow() == null ) return -1;
		getWindow().setSustainedPerformanceMode( enable );
		return 0;
	}
    
    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }

}
