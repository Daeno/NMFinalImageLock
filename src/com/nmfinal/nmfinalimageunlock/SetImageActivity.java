package com.nmfinal.nmfinalimageunlock;

import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class SetImageActivity extends Activity {
	private Uri fileUri;
	private static final int MEDIA_TYPE_IMAGE = 1;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private int MOSAIC_NUM = 8;
	private String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.nmfinal.nmfinalimageunlock/files/Pictures/PictureLock/";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setimage);
		findViews();
		addListeners();
		fileUri = getOutputMediaUri( MEDIA_TYPE_IMAGE );
		//native camera using
		Intent pictureIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
		pictureIntent.putExtra( MediaStore.EXTRA_OUTPUT, fileUri);
		startActivityForResult( pictureIntent , CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}
	protected void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		if ( requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE )
		{
		   if ( resultCode == RESULT_OK )
		   {
		   	try
		   	{
		   		Mat pass = new Mat();
		   		pass = Highgui.imread(path + "password.jpg");
		   		pass = mosaic(32,16,pass,false);
		   		Highgui.imwrite(path + "mosaic.jpg", pass);
		   		finish();
		   	}
		   	catch ( Exception e)
		   	{
		   		e.printStackTrace();
		   	}
		   }
		}
		if( resultCode == RESULT_CANCELED){
			finish();
		}
	}
	
	protected void findViews()
	{
	}
	
	protected void addListeners()
	{
	}
	
	
	private File getOutputMediaFile( int type )
	{		
		File mediaStorageDirectory = new File( getApplicationContext().getExternalFilesDir( Environment.DIRECTORY_PICTURES ), "PictureLock" );
		
		if ( !mediaStorageDirectory.exists() )
		{
			if ( !mediaStorageDirectory.mkdirs() )
			{
				Log.d( "PictureLock", "failed to create directory" );
				return null;
			}
		}
		
		//String timeStamp = new SimpleDateFormat( "yyMMdd_HHmmss").format( new Date() );
		File mediaFile = null;
		
		if ( type == MEDIA_TYPE_IMAGE ){
				mediaFile = new File( mediaStorageDirectory.getPath() + File.separator + "password.jpg" );
			
		}
			
		
		return mediaFile;
	}
	private Uri getOutputMediaUri( int type )
	{
		return Uri.fromFile( getOutputMediaFile( type ) );
	}
	
	private Mat mosaic(int tileCols, int tileRows, Mat image, boolean isBoundary){
    	int tileWidth = image.cols() / tileCols;
		int tileHeight = image.rows() / tileRows;
		Mat dotImage = new Mat();
		dotImage = image.clone();
		double[] temp, av;
		av = new double[4];
		av[0] = av[1] = av[2] = av [3] = 0;
		for(int y=0;y<tileRows;y++)
			for(int x=0;x<tileCols;x++){
				//color averaging
					
					for(int i=0;i<MOSAIC_NUM;i++){
						int dx = (int)(Math.random()*tileWidth);
						int dy = (int)(Math.random()*tileHeight);
						temp = image.get((y * tileHeight) + dy, (x * tileWidth) + dx);
						av[0] += temp[0];
						av[1] += temp[1];
						av[2] += temp[2];
					}
					av[0] /= MOSAIC_NUM;
					av[1] /= MOSAIC_NUM;
					av[2] /= MOSAIC_NUM;
				//----------------------------------------
				Scalar color = new Scalar(av[0],av[1],av[2]);
				//Log.i("color",color.toString());
				if(isBoundary)
					Core.rectangle(dotImage,new Point(x * tileWidth, y * tileHeight),new Point((x + 1) * tileWidth, (y + 1) * tileHeight),Scalar.all(-1));
				if(isBoundary)
					Core.rectangle(dotImage,new Point(x * tileWidth + 1, y * tileHeight + 1),new Point((x + 1) * tileWidth - 1, (y + 1) * tileHeight - 1),color,Core.FILLED);
				else
					Core.rectangle(dotImage,new Point(x * tileWidth, y * tileHeight),new Point((x + 1) * tileWidth, (y + 1) * tileHeight),color,Core.FILLED);
			}
		return dotImage;
    }
	
}
