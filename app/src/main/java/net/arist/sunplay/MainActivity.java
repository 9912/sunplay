package net.arist.sunplay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.os.Process;

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
	String afiles[];
	String svsong = "";
	int song_index = 0;
	TextView song_title;
	Context context;
	AssetManager assetManager;
	
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) 
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		SetSustainedPerformanceMode( true );
		int optimal_buffer_size = GetAudioOutputBufferSize();
		int optimal_sample_rate = GetAudioOutputSampleRate();
		int sample_rate = 44100;
		String cfg = "";
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

		song_title = new TextView(this);
		song_title.findViewById(R.id.songtitle);
		context = getApplicationContext();
		assetManager = context.getAssets();
		try{
			//song_title.setText(Integer.toString(assetManager.list("sv").length));
			//String svfiles[] = new String[assetManager.list("sv").length];
			afiles = assetManager.list("sv");
		} catch (IOException e) {
			Log.e( "SunVoxPlayer", "File list error" );
		}
		
		for (String i : afiles){
			if(i.endsWith(".sunvox")){
				svcount++;
			}
		}
		svfiles = new String[svcount];
		svcount = 0;
		for (String i : afiles){
			if(i.endsWith(".sunvox")){
				svfiles[svcount] = i;
				svcount++;
			}
		}
		((TextView) findViewById(R.id.songtitle)).setText(svfiles[0]);

		sunvox_version = SunVoxLib.init( cfg, sample_rate, 2, 0 );
		if( sunvox_version > 0 )
	    {
	        int major = ( sunvox_version >> 16 ) & 255;
	        int minor1 = ( sunvox_version >> 8 ) & 255;
	        int minor2 = ( sunvox_version ) & 255;
	        Log.i( "SunVoxPlayer", "SunVox lib version: " + major + " " + minor1 + " " + minor2 );
	        
	        //Open audio slot #0:
	        SunVoxLib.open_slot( 0 );
	        
	        //Load test song from raw resource:
	        byte[] song_data = null;
	        try {
				song_data = convertStreamToByteArray( assetManager.open( "sv/" + svfiles[0] ) );
			} catch( NotFoundException e ) {
				Log.e( "SunVoxPlayer", svfiles[0] + " not found" );
			} catch( IOException e ) {
				Log.e( "SunVoxPlayer", "Assets opening error" );
			}
	        if( song_data != null )
	        {
	        	int rv = SunVoxLib.load_from_memory( 0, song_data );
	        	if( rv == 0 )
	        		Log.i( "SunVoxPlayer", "Song loaded" );
	        	else
	        		Log.e( "SunVoxPlayer", "Song load error " + rv );
	        }	        
	    }
		else
		{
			Log.e( "SunVoxPlayer", "Can't open SunVox library" );
		}
	}
 /*
	@Override
	public boolean onCreateOptionsMenu( Menu menu ) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.activity_main, menu );
		return true;
	}
	*/
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

			/*
	        //Get some song info:
	        Log.i( "SunVoxPlayer", "Song name = " + SunVoxLib.get_song_name( 0 ) );
	        */

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
		byte[] song_data = null;
		try {
			song_data = convertStreamToByteArray( assetManager.open( "sv/" + svsong ));
		} catch( NotFoundException e ) {
			Log.e( "SunVoxPlayer", svsong + " not found" );
		} catch( IOException e ) {
			Log.e( "SunVoxPlayer", "openRawResource error" );
		}
		if( song_data != null )
		{
			int rv = SunVoxLib.load_from_memory( 0, song_data );
			if( rv == 0 )
				Log.i( "SunVoxPlayer", "Song loaded" );
			else
				Log.e( "SunVoxPlayer", "Song load error " + rv );
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
		byte[] song_data = null;
		try {
			song_data = convertStreamToByteArray( assetManager.open( "sv/" + svsong ));
		} catch( NotFoundException e ) {
			Log.e( "SunVoxPlayer", svsong + " not found" );
		} catch( IOException e ) {
			Log.e( "SunVoxPlayer", "openRawResource error" );
		}
		if( song_data != null )
		{
			int rv = SunVoxLib.load_from_memory( 0, song_data );
			if( rv == 0 )
				Log.i( "SunVoxPlayer", "Song loaded" );
			else
				Log.e( "SunVoxPlayer", "Song load error " + rv );
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

}
