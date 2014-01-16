package com.nmfinal.nmfinalimageunlock.camera;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.nmfinal.nmfinalimageunlock.R;
import com.nmfinal.nmfinalimageunlock.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class CameraActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = false;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 9000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	private static final String  TAG = "VVDPictureLock::Camera::Activity";
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int MEDIA_TYPE_IMAGE = 1;
	private int MOSAIC_NUM = 6;
	String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.nmfinal.nmfinalimageunlock/files/Pictures/PictureLock/";
	private Tutorial3View2 mOpenCvCameraView;
	private static boolean isSelecting = false;
	private static boolean isCropping = false;
	private static boolean isTracking = false;
	private static boolean isMosaic = false;
	private static int roiP1;
	private static int roiX1 = 0;
	private static int roiY1 = 0;
	private static int roiX2 = 0;
	private static int roiY2 = 0;
	private float xOffset;
	private float yOffset;
	private Mat frame;
	private Mat template;
	private Mat previousFrame;
	private Mat flow;
	private Mat cflow;
	private MatOfPoint2f prevPointsOfInterest;
	private MatOfPoint2f currpointsOfInterest;
	TermCriteria termcrit;
	Size subPixWinSize;
	Size winSize;
	Rect roi;
	boolean needToInit = true;
	private final int MAXCOUNT = 500;
	private boolean frameOpt = false;
	//private String gesturePassword;
	private static ArrayList<Point> gesturePointList;
	private static List<Double> gestureSlopeList;
	double psnrV;
	Scalar mssimV;
   	String password = "/storage/emulated/0/Android/data/com.nmfinal.nmfinalimageunlock/files/Pictures/PictureLock/IMG_140112_084725.jpg";
	String gesturePassword = "/storage/emulated/0/Android/data/com.nmfinal.nmfinalimageunlock/files/Pictures/PictureLock/Gesture_140115_154919.dat";
  	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

	       @Override
	       public void onManagerConnected(int status) {
	           switch (status) {
	               case LoaderCallbackInterface.SUCCESS:
	               {
	                   Log.i(TAG, "OpenCV loaded successfully");

	                   mOpenCvCameraView.enableView();
	                   //mOpenCvCameraView.enableFpsMeter();
	                   mOpenCvCameraView.setOnTouchListener( CameraActivity.this );
	               } break;
	               default:
	               {
	                   super.onManagerConnected(status);
	               } break;
	           }
	       }
	   };
	   public android.view.View.OnClickListener mosaicButtonOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v){
				if(!isMosaic){
					isMosaic = true;
				}else{
					//if(MOSAIC_NUM > 64){
						MOSAIC_NUM = 8;
						isMosaic = false;
					/*}else{
						MOSAIC_NUM += 8;
					}*/
				}
			}
		};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_camera);

		/*final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.tutorial3_activity_java_surface_view2);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.trackButton).setOnTouchListener(
				mDelayHideTouchListener);
		*/
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		mOpenCvCameraView = (Tutorial3View2) findViewById(R.id.tutorial3_activity_java_surface_view2);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		Button trackBtn = ( Button ) findViewById( R.id.trackButton );
		Button saveBtn = ( Button ) findViewById( R.id.saveButton );
		Button checkBtn = ( Button ) findViewById( R.id.checkButton );
		
		trackBtn.setVisibility( Button.VISIBLE );
		trackBtn.setOnClickListener( trackButtonOnClickListener );
		saveBtn.setVisibility( Button.VISIBLE );
		saveBtn.setOnClickListener( saveButtonOnClickListener );
		checkBtn.setVisibility( Button.VISIBLE );
		checkBtn.setOnClickListener( checkButtonOnClickListener );
		
		Button mosaicBtn = ( Button ) findViewById (R.id.mosaicButton);
		mosaicBtn.setVisibility(Button.VISIBLE);
		mosaicBtn.setOnClickListener(mosaicButtonOnClickListener);
		
		Button shotBtn = (Button) findViewById(R.id.shotButton);
		shotBtn.setVisibility(Button.VISIBLE);
		shotBtn.setOnClickListener(shotButtonOnClickListener);
		mOpenCvCameraView.setMaxFrameSize(640, 480);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		//delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	/*View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	/*private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}*/
	private void templateMatching( int type )
	{
		int resCols = frame.cols() - template.cols() + 1;
		int resRows = frame.rows() - template.rows() + 1;
		Mat result = new Mat();
		
		result.create( resCols, resRows, CvType.CV_32FC1 );
		Imgproc.matchTemplate( frame, template, result, type );
		Core.normalize( result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat() );
		
		Point matchLocation;
		Core.MinMaxLocResult mmr = Core.minMaxLoc( result );
		
		if ( type == Imgproc.TM_SQDIFF || type == Imgproc.TM_SQDIFF_NORMED )
			matchLocation = mmr.minLoc;
		else
			matchLocation = mmr.maxLoc;
		
		
		Point oppMatch = new Point( matchLocation.x + template.cols(), matchLocation.y + template.rows() );
		template = frame.submat( new Rect( matchLocation, oppMatch ) );
		Core.rectangle( frame, matchLocation, oppMatch, new Scalar( 0, 0, 255 ), 2, 8, 0 );		
		gesturePointList.add( matchLocation );
	}
	
	private void fastTemplateMatching( Mat grayRef, int maxLevel )
	{
		ArrayList<Mat> references = new ArrayList<Mat>();
		ArrayList<Mat> templates = new ArrayList<Mat>();
		ArrayList<Mat> results = new ArrayList<Mat>();
		
		buildPyramid( grayRef, references, maxLevel );
		buildPyramid( template, templates, maxLevel );
		
		Mat ref = new Mat();
		Mat tpl = new Mat();
		Mat res = new Mat();
		
		for ( int level = maxLevel; level >= 0; level-- )
		{
			ref = references.get( level );
			tpl = templates.get( level );
			res = Mat.zeros( ref.rows() - tpl.rows() + 1, ref.cols() - tpl.cols() + 1, CvType.CV_32FC1 );
			
			if ( level == maxLevel )
				Imgproc.matchTemplate( ref, tpl, res, Imgproc.TM_CCORR_NORMED );
			else
			{
				Mat mask = new Mat();
				Imgproc.pyrUp( results.get( results.size() - 1 ), mask );
				
				Mat mask8U = new Mat();
				mask.convertTo( mask8U, CvType.CV_8U );
				
				List< MatOfPoint > contours = new ArrayList< MatOfPoint >();
				
				Imgproc.findContours( mask8U, contours, new Mat(),Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE );
				
				for ( int i = 0; i < contours.size(); i++ )
				{
					Rect r = Imgproc.boundingRect( contours.get( i ) );
					Imgproc.matchTemplate( ref.submat( r.y, r.y + r.height + tpl.rows() - 1, r.x, r.x + r.width + tpl.cols() - 1 ), tpl, res.submat( r ), Imgproc.TM_CCORR_NORMED );
				}
			}
			
			Imgproc.threshold( res, res, 0.94, 1., Imgproc.THRESH_TOZERO );
			results.add( res );
		}
		
		Point matchLocation;
		Core.MinMaxLocResult mmr = Core.minMaxLoc( res );
		
		matchLocation = mmr.maxLoc;	
		Point oppMatch = new Point( matchLocation.x + template.cols(), matchLocation.y + template.rows() );
		template = grayRef.submat( new Rect( matchLocation, oppMatch ) );
		Core.rectangle( frame, matchLocation, oppMatch, new Scalar( 0, 0, 255 ), 2, 8, 0 );		
		gesturePointList.add( matchLocation );
	}
	
	private void buildPyramid( Mat src, ArrayList<Mat> dsts, int maxLevel )
	{
		dsts.add( src );
		
		if ( maxLevel > 0 )
		{
			Mat nextLevel = new Mat();
			
			Imgproc.pyrDown( src, nextLevel );
			buildPyramid( nextLevel, dsts, maxLevel - 1 );
		}
	}
	
	private void opticalFlowLKTracking( Mat currentFrame )
	{
		
		if ( needToInit )
		{
			/*MatOfPoint initial = new MatOfPoint();
			Imgproc.goodFeaturesToTrack( currentFrame, initial, MAXCOUNT, 0.1, 10.0, new Mat(), 3, false, 0.04 );
			currpointsOfInterest.fromArray( initial.toArray() );
			Imgproc.cornerSubPix( currentFrame, currpointsOfInterest, subPixWinSize, new Size( -1, -1 ), termcrit );*/
			ArrayList<Point> targetPoints = new ArrayList<Point>();
			previousFrame = new Mat();
			prevPointsOfInterest = new MatOfPoint2f();
			double centerX = ( roiX1 + roiX2 ) / 2;
			double centerY = ( roiY1 + roiY2 ) / 2;
			
			for ( int i = -5; i <= 5; i++ )
			{
				for ( int j = -5; j <= 5; j++ )
				{
					targetPoints.add( new Point( centerX + i, centerY + j ) );
				}
			}
			
			currpointsOfInterest.fromList( targetPoints );
			Imgproc.cornerSubPix( currentFrame, currpointsOfInterest, subPixWinSize, new Size( -1, -1 ), termcrit );
			needToInit = false;
		}
		else if ( ! prevPointsOfInterest.empty() )
		{
			MatOfByte status = new MatOfByte();
			MatOfFloat err = new MatOfFloat();
			int capacity = 0;
			
			if ( previousFrame.empty() )
				currentFrame.copyTo( previousFrame );
			
			Log.i("test", "pF: " + previousFrame.size() + " cF: " + currentFrame.size() + "pp: " + prevPointsOfInterest.size() + "cp: " + currpointsOfInterest.size() );
			Video.calcOpticalFlowPyrLK( previousFrame, currentFrame, prevPointsOfInterest, currpointsOfInterest, status, err, winSize, 3, termcrit, Video.OPTFLOW_LK_GET_MIN_EIGENVALS, 0.0001 );
			
			ArrayList<Point> currPOI = new ArrayList<Point>( currpointsOfInterest.toList() );
			byte[] statusArray = status.toArray();
			
			for ( int i = 0; i < currPOI.size(); i++ )
			{
				if ( statusArray[ i ] != 0  )
				{
					currPOI.set( capacity++, currPOI.get( i ) );
					Core.circle( frame, currPOI.get( i ), 3, new Scalar( 0, 255, 0 ), -1 );
				}
			}						
			
			Log.i("test", "capcity: " + capacity );
			
			if ( capacity != 0 )
			{
				currPOI = resizeArray( currPOI, capacity );
				currpointsOfInterest.fromList( currPOI ); //
				MatOfPoint temp = new MatOfPoint( currpointsOfInterest.toArray() ); 
				Rect r = Imgproc.boundingRect( temp );
				roiX1 = ( int ) r.tl().x;
				roiY1 = ( int ) r.tl().y;
				roiX2 = ( int ) r.br().x;
				roiY2 = ( int ) r.br().y;
				Core.rectangle( frame, r.tl(), r.br(), new Scalar( 0, 0, 255 ) );
			}
			else
				currpointsOfInterest = new MatOfPoint2f();
		}
		
		MatOfPoint2f tmp = new MatOfPoint2f( prevPointsOfInterest.toArray() );
		prevPointsOfInterest = currpointsOfInterest;
		currpointsOfInterest = tmp;
		previousFrame = currentFrame;		
	}
	
	private void opticalFlowFBTracking( Mat currentFrame )
	{
		int buffSize = 25; 
		Rect buff = new Rect( new Point( roi.tl().x - buffSize, roi.tl().y - buffSize ) , new Point( roi.br().x + buffSize, roi.br().y + buffSize ) );
		Mat bufferFrame = currentFrame.submat( buff );
		Mat roiFrame = currentFrame.submat( roi );
		
		if ( ! previousFrame.empty() )
		{
			Video.calcOpticalFlowFarneback( previousFrame, currentFrame, flow, 0.5, 3, 15, 3, 5, 1.2, 0 );
			Imgproc.cvtColor( previousFrame, frame, Imgproc.COLOR_GRAY2BGR );
			drawOptFlowMap( flow, frame, 4, 1.5, new Scalar( 255 , 0, 0 ) );
		}
		
		previousFrame =currentFrame;
	}
	
	private void drawOptFlowMap( Mat flow, Mat cflowMap, int buffSize, double scale, Scalar color )
	{
		//ArrayList<double[]> temp = new ArrayList<double[]>( 4 );
		int centerX = (int) ( ( roi.tl().x + roi.br().x ) / 2 );
		int centerY = (int) ( ( roi.tl().y + roi.br().y ) / 2 );
		Point fxy = new Point( flow.get( centerY, centerX ) );
		roi = new Rect( new Point( roi.tl().x + fxy.x, roi.tl().y + fxy.y ), roi.size() );
		Core.rectangle( frame, roi.tl(), roi.br(), color );
		Log.i("test", "fxy" + fxy );
		Log.i("test", "roi.ti()" + roi.tl() + "roi.br()" + roi.br() );
		/*temp.add( flow.get( buffSize, buffSize ) );
		temp.add( flow.get( buffSize, buffSize + roi.width ) );
		temp.add( flow.get( buffSize + roi.height, buffSize ) );
		temp.add( flow.get( buffSize + roi.height, buffSize + roi.width ) );*/
		
		
		/*for ( int y = 0; y < cflowMap.rows(); y += step )
		{
			for ( int x = 0; x < cflowMap.cols(); x += step )
			{
				double[] buff = flow.get( y, x );
				Point fxy = new Point( buff );
				Core.line( cflowMap, new Point( x, y ), new Point( Math.round( x + fxy.x ), Math.round( y + fxy.y) ), color );
				Core.circle( cflowMap, new Point( x, y), 2, color, -1 );
			}
		}*/
	}
	
	private void saveGesture()
	{
		File mediaStorageDirectory = new File( getApplicationContext().getExternalFilesDir( Environment.DIRECTORY_PICTURES ), "VVDPictureLock" );
		String timeStamp = new SimpleDateFormat( "yyMMdd_HHmmss").format( new Date() );		
		
		try
		{
			String fileName = mediaStorageDirectory.getPath() + File.separator + "Gesture_" + timeStamp + ".dat";
			FileOutputStream fos = new FileOutputStream( fileName );
			BufferedOutputStream bos = new BufferedOutputStream( fos );
			DataOutputStream dos = new DataOutputStream( bos );
			
			for ( int i = 0; i < gestureSlopeList.size(); i++ )
			{
				dos.writeDouble( gestureSlopeList.get( i ) );
			}
			
			Log.i( "test", fileName );
			gesturePassword = fileName;
			dos.flush();
			dos.close();
		}
		catch ( Exception e )
		{
			
		}
	}
	
	private List<Double> loadGesture()
	{
		List<Double> slopeSequence = new ArrayList<Double>();
		FileInputStream fis;
		BufferedInputStream bis;
		DataInputStream dis;
		
		try
		{
			fis = new FileInputStream( gesturePassword );
			bis = new BufferedInputStream( fis );
			dis = new DataInputStream( bis );
					
			while ( dis.available() > 0 )
			{
				slopeSequence.add( dis.readDouble() );
			}
			
			dis.close();
		}
		catch ( Exception e )
		{
			
		}
		
		return slopeSequence;
	}
	
	private void recognizeGesture()
	{
		List<Double> passwordSequence = loadGesture();
		double speedRatioStoP = ( double ) ( gestureSlopeList.size() - 1 ) / ( double ) ( passwordSequence.size() - 1 ); 
		double accessPercentage = 0.5;
		double quantizedSlopeP;    
		double quantizedSlopeS;
		int effectiveIndex = (int) ( Math.rint( speedRatioStoP * ( passwordSequence.size() - 1 ) ) );
		int matchCount = 0;
		int limit = 0;
		
		Log.i("test", "passwordSequence.size(): "  + passwordSequence.size() );
		Log.i("test", "gestureSlopeList.size(): "  + gestureSlopeList.size() );
		Log.i("test", "speedRatioStoP: "  + speedRatioStoP );
		Log.i("test", "effectiveIndex: "  + effectiveIndex );
		
		if (  effectiveIndex < ( gestureSlopeList.size() - 1 ) )
		{
			Log.i("test", "up" );
			limit = passwordSequence.size();
			
			for ( int i = 0; i < limit && effectiveIndex < gestureSlopeList.size(); i++ )
			{
				effectiveIndex = (int) Math.rint( i * speedRatioStoP );			
				quantizedSlopeP = Math.atan( passwordSequence.get( i ) );
				quantizedSlopeS = Math.atan( gestureSlopeList.get( effectiveIndex ) );
				Log.i("test", "quantizedSlopeP: " + quantizedSlopeP );
				Log.i("test", "quantizedSlopeS: " + quantizedSlopeS );
					
			   if ( Math.abs( quantizedSlopeS - quantizedSlopeP ) < 0.2 )
			   {
			   	matchCount++;
			   	Log.i("test", "match: " + i + " " + effectiveIndex + "!" );
			   }
			}			
		}
		else
		{
			Log.i("test", "down" );
			limit = gestureSlopeList.size();
			effectiveIndex = 0;
			
			for ( int i = 0; effectiveIndex < limit && i < passwordSequence.size(); i++ )
			{
				quantizedSlopeP = Math.atan( passwordSequence.get( i ) );
				quantizedSlopeS = Math.atan( gestureSlopeList.get( effectiveIndex ) );
				effectiveIndex = (int) Math.rint( ( i + 1 ) * speedRatioStoP );
				Log.i("test", "quantizedSlopeP: " + quantizedSlopeP );
				Log.i("test", "quantizedSlopeS: " + quantizedSlopeS );
				
			   if ( Math.abs( quantizedSlopeS - quantizedSlopeP ) < 0.2 )
			   {
			   	matchCount++;
			   	Log.i("test", "match: " + i + " " + effectiveIndex );
			   }
			}
			
		}
		
		Log.i("test", "matchCount: " + matchCount );
		Log.i("test", "limit: " + limit );
		double succeedRatio =  ( double ) matchCount / ( double ) limit;
		View v = findViewById(R.id.tutorial3_activity_java_surface_view );
		
		if ( succeedRatio >= accessPercentage )
			Toast.makeText( v.getContext(), "pass!!", Toast.LENGTH_LONG).show();
		else
			Toast.makeText( v.getContext(), "you shall not pass!!", Toast.LENGTH_LONG).show();
	}
	
	private void convertGesture()
	{
		Point p1;
		Point p2;
		double leftSlope = 0;
		double rightSlope = 0;
		
		if ( gesturePointList.size() > 1 )   ////////////////
		{
			gestureSlopeList = new ArrayList<Double>();
			p1 = gesturePointList.get( 0 );
			p2 = gesturePointList.get( 1 );
			
			if ( p2.x - p1.x != 0 )
				gestureSlopeList.add( ( p2.y - p1.y ) / ( p2.x - p1.x ) );
			else
				gestureSlopeList.add( Double.MAX_VALUE );
			
			
			for ( int i = 1; i < gesturePointList.size() - 1; i++ )
			{
				p1 = gesturePointList.get( i );
				p2 = gesturePointList.get( i + 1 );
				leftSlope = gestureSlopeList.get( i - 1 );
				
				if ( p2.x - p1.x != 0 )
					rightSlope = ( p2.y - p1.y ) / ( p2.x - p1.x );
				else
					rightSlope = Double.MAX_VALUE;
				
				gestureSlopeList.add( ( leftSlope + rightSlope ) / 2 );
			}
			
			gestureSlopeList.add( rightSlope );
		}
	}
	
	private ArrayList<Point> resizeArray( ArrayList<Point> arr, int capacity )
	{
		ArrayList<Point> kList = new ArrayList<Point>( capacity );
		
		for ( int i = 0; i < capacity; i++ )
		{
			kList.add( arr.get( i ) );
		}
		
		return kList;
	}
	
	
	public android.view.View.OnClickListener trackButtonOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) 
		{
			if ( ! isTracking )
			{
				if ( isCropping )
				{
					if ( roiX1 < roiX2 )
					{
						if ( roiY1 < roiY2 )
							template = frame.submat( roiY1, roiY2, roiX1, roiX2 );
						else
							template = frame.submat( roiY2, roiY1, roiX1, roiX2 );
					}
					else
					{
						if ( roiY1 < roiY2 )
							template = frame.submat( roiY1, roiY2, roiX2, roiX1 );
						else
							template = frame.submat( roiY2, roiY1, roiX2, roiX1 );
					}
					Log.i(TAG, template.size().toString());
					isTracking = true;
					Imgproc.cvtColor( template, template, Imgproc.COLOR_BGR2GRAY );
					gesturePointList = new ArrayList<Point>();
					currpointsOfInterest = new MatOfPoint2f();
					termcrit = new TermCriteria( TermCriteria.MAX_ITER|TermCriteria.EPS, 20, 0.03 );
					subPixWinSize = new Size( 10, 10 );
					winSize = new Size( 31, 31 );
					previousFrame = new Mat();
					flow = new Mat();
					cflow = new Mat();
					Point p1 = new Point( roiX1, roiY1 );
					Point p2 = new Point( roiX2, roiY2 );
					roi = new Rect( p1, p2 );
				}
			}
			else
			{
				//template = null;
				isCropping = false;
				isTracking = false;
				convertGesture();
			}			
		}
	}; 
	
	public android.view.View.OnClickListener saveButtonOnClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				if ( ! isTracking )
					saveGesture();
			}
	};
	public android.view.View.OnClickListener shotButtonOnClickListener = new OnClickListener(){
		@Override
		public void onClick(View V){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	        String currentDateandTime = sdf.format(new Date());
	        String fileName = path + currentDateandTime + ".jpg";
	        mOpenCvCameraView.takePicture(fileName);
	        Toast.makeText(CameraActivity.this, fileName + " saved", Toast.LENGTH_SHORT).show();
		}
	};
	public android.view.View.OnClickListener checkButtonOnClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				if ( ! isTracking )	
					recognizeGesture();
			}
		};
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if ( ! isTracking )
		{
			int cols = frame.cols();
	      int rows = frame.rows();
	      
	      if( !frameOpt )
	      {
	      	xOffset = (float)cols/(float)mOpenCvCameraView.getWidth();
	      	yOffset = (float)rows/(float)mOpenCvCameraView.getHeight();
	      	frameOpt = true;
	      }
	
	      int x = (int)(event.getX() * xOffset);
	      int y = (int)(event.getY() * yOffset);
			int type = event.getAction();
			
			if ( type == android.view.MotionEvent.ACTION_MOVE )
			{
				if ( isSelecting )
				{
				   roiX2 = x;
				   roiY2 = y;
				}
			}
			else if ( type == android.view.MotionEvent.ACTION_DOWN )
			{
			   roiX1 = x;
			   roiX2 = x;
			   roiY1 = y;		   
			   roiY2 = y;
			   isSelecting = true;
			   isCropping = true;
			}
			else if ( type == android.view.MotionEvent.ACTION_UP )
			{
				isSelecting = false;
				
				if(Math.abs(roiX1 - roiX2) <= 4 || Math.abs(roiY2-roiY1)<= 4)
	        		isCropping = false;
				
				return false;
			}
			
		   return true;
		}
		
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
	   	 
	   	 frame = inputFrame.rgba();
	   	 
	   	 if( isCropping )
	   	 {
	   		 if ( ! isTracking )
	   		 {
		   		 Point p1 = new Point( roiX1, roiY1 );
		   		 Point p2 = new Point( roiX2, roiY2 );
		   		 Core.rectangle( frame, p1, p2, new Scalar( 0, 0, 255 ), 2 );
	   		 }
	   		 else
	   		 {
	   			 //templateMatching( Imgproc.TM_SQDIFF_NORMED );
	   			 Mat grayRef = new Mat();
	   			 
	   			// long StartTime = System.currentTimeMillis();
	   			 Imgproc.cvtColor( frame, grayRef, Imgproc.COLOR_BGR2GRAY );		 
	   			 //fastTemplateMatching( grayRef, 2 );
	   			 //opticalFlowLKTracking( grayRef );
	   			 opticalFlowFBTracking( grayRef );
	   			 System.gc();
			   	// long EndTime = System.currentTimeMillis();
			   	// long ExecutionTime = EndTime - StartTime;
			   	// Log.i("abcdef", "Time: " + ExecutionTime );   			 
	   		 }
	   	 }
	   	if(isMosaic){
			mosaic(32,16,frame,false);
		}
	   	 return frame;
	   }
	
	@Override
    public void onPause()
    {
        super.onPause();
        Log.i("camere","pause");
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
    	Log.i("camera","OnResume");
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        finish();
    }
    
    private void mosaic(int tileCols, int tileRows, Mat image, boolean isBoundary){
    	int tileWidth = image.cols() / tileCols;
		int tileHeight = image.rows() / tileRows;
		//Mat dotImage = new Mat();
		//dotImage = image.clone();
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
					Core.rectangle(frame,new Point(x * tileWidth, y * tileHeight),new Point((x + 1) * tileWidth, (y + 1) * tileHeight),Scalar.all(-1));
				if(isBoundary)
					Core.rectangle(frame,new Point(x * tileWidth + 1, y * tileHeight + 1),new Point((x + 1) * tileWidth - 1, (y + 1) * tileHeight - 1),color,Core.FILLED);
				else
					Core.rectangle(frame,new Point(x * tileWidth, y * tileHeight),new Point((x + 1) * tileWidth, (y + 1) * tileHeight),color,Core.FILLED);
			}
		//return dotImage;
    }
	
	
}
