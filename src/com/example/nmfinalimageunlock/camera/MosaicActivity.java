package com.example.nmfinalimageunlock.camera;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import com.nmfinal.nmfinalimageunlock.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

public class MosaicActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
	private String TAG = "VVDPictureLock::MosaicActivity";
	private Tutorial3View mOpenCvCameraView;
	private String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.example.vvdpicturelock/files/Pictures/VVDPictureLock/";
	
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
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mosaic);
		mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
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
		getMenuInflater().inflate(R.menu.mosaic, menu);
		return true;
	}
	
	private void findView(){
		ImageView mo = (ImageView) findViewById(R.id.mosaicImage);
		Matrix matrix = new Matrix();
		Bitmap bitmapOrg = BitmapFactory.decodeFile(path + "Mosaic.jpg");
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

	
	@SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG,"onTouch event");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = path + "sample_picture_" + currentDateandTime + ".jpg";
        mOpenCvCameraView.takePicture(fileName);
        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
        finish();
        return false;
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
