package com.nmfinal.nmfinalimageunlock.camera;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.nmfinal.nmfinalimageunlock.R;

public class MosaicActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
	private String TAG = "VVDPictureLock::MosaicActivity";
	private Tutorial3View mOpenCvCameraView;
	private String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.nmfinal.nmfinalimageunlock/files/Pictures/PictureLock/";
	private Uri saveUri;
	private boolean isShowing = false;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

	       @Override
	       public void onManagerConnected(int status) {
	           switch (status) {
	               case LoaderCallbackInterface.SUCCESS:
	               {
	                   Log.i(TAG, "OpenCV loaded successfully");
	                   mOpenCvCameraView.enableView();
	                   //mOpenCvCameraView.enableFpsMeter();
	                   mOpenCvCameraView.setOnTouchListener( MosaicActivity.this );
	               } break;
	               default:
	               {
	                   super.onManagerConnected(status);
	               } break;
	           }
	       }
	   };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mosaic);
		mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		saveUri = (Uri) getIntent().getExtras().getParcelable(MediaStore.EXTRA_OUTPUT);

		findView();
	}
	@Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.mosaic, menu);
		return true;
	}
	
	private void findView(){
		if(isShowing){
			ImageView mo = (ImageView) findViewById(R.id.mosaicImage);
			Matrix matrix = new Matrix();
			File ff = new File(path + "Mosaic.jpg");
			if(ff.exists()){
			Bitmap bitmapOrg = BitmapFactory.decodeFile(path + "mosaic.jpg");
			matrix.postRotate(-90);
			int height,width;
			height = bitmapOrg.getHeight();
			width = bitmapOrg.getWidth();
			if(height > width){
				//Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapOrg,height,width, true);
	
				Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapOrg , 0, 0, bitmapOrg .getWidth(), bitmapOrg .getHeight(), matrix, true);
				//mo.setImageURI(uri);
				mo.setImageBitmap(rotatedBitmap);
			}else{
				mo.setImageBitmap(bitmapOrg);
			}
			mo.setVisibility(ImageView.VISIBLE);
			}
			else{
				mo.setVisibility(ImageView.INVISIBLE);
				Log.i("image","load fail");
			}
		}
	}

	
	@SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG,"onTouch event: " + Integer.toString(event.getAction() )  );
        if(event.getAction() == MotionEvent.ACTION_MOVE){
        	if(!isShowing){
        		isShowing = true;
        		findView();
        	}
        	return true;
        }
        if(event.getAction() == MotionEvent.ACTION_UP){
	        if (saveUri != null)
			{
			    // Save the bitmap to the specified URI (use a try/catch block)
			    /*outputStream = getContentResolver().openOutputStream(saveUri);
			    outputStream.write(data); // write your bitmap here
			    outputStream.close();*/
	        	isShowing = false;
	        	findView();
	        	String fileName = saveUri.getPath();
	        	mOpenCvCameraView.takePicture(fileName);
	        	Toast.makeText(this, "Picture Captured", Toast.LENGTH_SHORT);
			    setResult(RESULT_OK);
			    finish();
			}
			else
			{
			    // If the intent doesn't contain an URI, send the bitmap as a Parcelable
			    // (it is a good idea to reduce its size to ~50k pixels before)
			    //setResult(RESULT_OK, new Intent("inline-data").putExtra("data", bitmap));
				setResult(RESULT_CANCELED);
				finish();
			}
	        return false;
        }
        /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = path + "sample_picture_" + currentDateandTime + ".jpg";
        mOpenCvCameraView.takePicture(fileName);
        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
        finish();*/
        return true;
    }

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// TODO Auto-generated method stub
		return inputFrame.rgba();
	}

}
