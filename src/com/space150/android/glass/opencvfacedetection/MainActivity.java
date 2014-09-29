package com.space150.android.glass.opencvfacedetection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;



public class MainActivity extends Activity implements CvCameraViewListener2, GestureDetector.OnGestureListener, SurfaceHolder.Callback,PreviewCallback, PictureCallback{

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 0, 255, 128);

    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private CameraBridgeViewBase   mOpenCvCameraView;
    //private CameraControl mOpenCvCameraView;

    private GestureDetector mGestureDetector;    
    private Camera mCamera;
    //private SurfaceHolder surfaceHolder;
    //private boolean mCameraConfigured = false;
    private boolean bAfterInit = false;
    private CameraControl mCameraControl;
    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_THRESHOLD_VELOCITY = 1000;
    private Bitmap bmp;
    
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
//                    
                    Log.i(TAG, "AFTER initialize Camera ====");
 
                    //////////////////////////////////////
                    //if (!qOpened){
                    
                   
//                    mOpenCvCameraView.enableView();
//                    mOpenCvCameraView.enableFpsMeter();
                    mOpenCvCameraView.enableView();
                    Log.i(TAG, "after enable view");
                    mCameraControl.setParam();
                    //mCameraControl.enableFpsMeter();
                    //mOpenCvCameraView.getHolder();
                       
                       
                        
                        bAfterInit = true;
                    
                    /// harry: why still can't get mCamera 
                    //} else {
                    	//mCamera.startPreview();
                    //}
                    
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    /**/
    //////////////////////////////////////////////////////
//    private boolean safeCameraOpen(int id) {
//        try {
//            releaseCameraAndPreview();
//            mCamera = Camera.open(id);
//            qOpened = (mCamera != null);
//        } catch (Exception e) {
//            Log.e(TAG, "failed to open Camera");
//            e.printStackTrace();
//        }
//
//        return qOpened;    
//    }
//    
//    private void releaseCameraAndPreview() {
//        //mPreview.setCamera(null);
//        if (mCamera != null) {
//            mCamera.release();
//            mCamera = null;
//        }
//    }
    //////////////////////////////////////////////

    public MainActivity()
    {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        
        mGestureDetector = new GestureDetector(this, this);   
        
/////////////
//        surfaceView = (SurfaceView)findViewById(R.id.fd_activity_surface_view);
//        surfaceHolder = surfaceView.getHolder();
//        surfaceHolder.addCallback(this);
        
        //mamahow:+++
        mOpenCvCameraView =(CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        SurfaceHolder surfaceHolder = mOpenCvCameraView.getHolder();
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
//        //mamahow ---
        surfaceHolder.addCallback(this);
        mOpenCvCameraView.setCvCameraViewListener(this);
////       
        
        ///////////////////////////////////////////////
        mCameraControl = (CameraControl) findViewById(R.id.fd_activity_camera_control);
        SurfaceHolder surfaceHolder2 = mCameraControl.getHolder();
        surfaceHolder2.addCallback(this);
        //mCameraControl.setVisibility(SurfaceView.VISIBLE);
        mCameraControl.setCvCameraViewListener(this);
      
        ////////////////////// example from example 3
        //mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);
        //mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        //mOpenCvCameraView.setCvCameraViewListener(this);
    }

    
    @Override
    public void onPause()
    {
    	Log.i(TAG, "+++++++++++++++ onPause called +++++++++++++++");
        
    	super.onPause();
    	//releaseCameraAndPreview();
    }	

    @Override
    public void onResume()
    {
    	Log.i(TAG, "+++++++++++++++ onResume called +++++++++++++++");
        super.onResume();
        //safeCameraOpen(0);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
      
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
        //mCameraControl.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    	
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    	

    	
    	
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        //Imgproc.equalizeHist(mGray, mGray);
        //Imgproc.GaussianBlur(mGray, mGray, new Size(5, 5), 0.0);
        
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();

        if (mJavaDetector != null)
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++){
        	
        	Log.i(TAG, "+++++++++++++++facesArray: number: "+facesArray.length);
        	
        	// mamahow: change to draw face
//        	Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
//        	
//        	
//        	Mat test = Highgui.imread("/mnt/sdcard/Pictures/head01.png");
//        	
        	
        	try {
                //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);
                //Imgproc.cvtColor(mRgba, tmp, Imgproc.COLOR_RGBA2BGRA, 4);
                //bmp = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
                //BitmapFactory  bm;
                //bmp = BitmapFactory.decodeFile("/mnt/sdcard/Pictures/head03.png"); //d:not hard code
                ImageView Img = (ImageView)findViewById(R.id.imageView1);
                //Img.setVisibility(View.INVISIBLE);
                
                //LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(Img.getLayoutParams());
                //RelativeLayout.LayoutParams mp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
 //               FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(Img.getLayoutParams()); 
                Log.i(TAG, "facesArray pos: "+(int)facesArray[i].tl().x+ "," +(int)facesArray[i].tl().y+ ","+ (int)facesArray[i].br().x+ ","+ (int)facesArray[i].br().y);
 //               fp.setMargins((int)facesArray[i].tl().x, (int)facesArray[i].tl().y, (int)facesArray[i].br().x, (int)facesArray[i].br().y);
                
                //Img.setImageBitmap(bmp);
                //Img.setLayoutParams(fp);
                
                //Img.setVisibility(View.INVISIBLE);
                
                //Img.setLayoutParams(new FrameLayout.LayoutParams(427/2, 648/2));
                Img.getLayoutParams().width = (int)facesArray[i].tl().x - (int)facesArray[i].br().x;
                Img.getLayoutParams().height = (int)facesArray[i].tl().y - (int)facesArray[i].br().y;
                
 
                //Img.offsetLeftAndRight(300);// doesn't work
                Img.setImageBitmap(bmp);
   //             Img.setLayoutParams(fp);
                // how green square
                Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
    
                Point a = new Point(facesArray[i].tl().x+20, facesArray[i].tl().y+20);
                Point b = new Point(facesArray[i].tl().x+40, facesArray[i].tl().y+20+20);
//                
//                Core.line(mRgba, a, b, FACE_RECT_COLOR, 3);
                
                Core.rectangle(mRgba, a, b, FACE_RECT_COLOR, 5);
                
                Point c = new Point(facesArray[i].tl().x+20+40, facesArray[i].tl().y+20);
                Point d = new Point(facesArray[i].tl().x+40+40, facesArray[i].tl().y+20+20);
                
                Core.rectangle(mRgba, c, d, FACE_RECT_COLOR, 5);
                              
     
                Point e = new Point(facesArray[i].br().x-40, facesArray[i].br().y-20);
                Point f = new Point(facesArray[i].br().x, facesArray[i].br().y-20);
                Core.line(mRgba, e, f, FACE_RECT_COLOR, 3);
                
                
//                function setPos(newX, newY, view) {
//                    view.top = newY - view.height/2;
//                    view.left = newX - view.width/2;
//                }

            }
            catch (Exception e){Log.d("Exception",e.getMessage());}
        	
        	
        	
        	
        	
        	
         	
        	
        	///mnt/sdcard/Pictures/head01.png
        	
//        	Mat b = null;
//        	// Small watermark image
//        	Mat a = Highgui.imread("/mnt/sdcard/Pictures/head01.png");
//
//        	Mat bSubmat = b.submat((int)facesArray[i].tl().x, (int)facesArray[i].tl().y, (int)facesArray[i].br().x, (int)facesArray[i].br().y);        
//        	a.copyTo(bSubmat);
//
//        	Highgui.imwrite("mnt/sdcard/SubmatCopyToTest.png", b);
        }
          
        return mRgba;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
        
        Toast.makeText(this, String.format("Face size: %.0f%%", mRelativeFaceSize*100.0f), Toast.LENGTH_SHORT).show();
    }
    

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
	
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
	{
		
		
		float size = mRelativeFaceSize;
		try {
            float totalXTraveled = e2.getX() - e1.getX();
            float totalYTraveled = e2.getY() - e1.getY();
            if (Math.abs(totalXTraveled) > Math.abs(totalYTraveled)) {
                if (Math.abs(totalXTraveled) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if (totalXTraveled > 10) {
                        Log.d("Event", "On Fling Forward ----");
                        size -= 0.2f;
            			if ( size < 0.2f )
            				size = 0.2f;
                        //
                    } else {
                        Log.d("Event", "On Fling Backward +++");
                        //
                        size += 0.2f;
            			if ( size > 0.8f )
            				size = 0.8f;
                    }
                }
            } else {
                if (Math.abs(totalYTraveled) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    if(totalYTraveled > 0) {
                    	// to leave app safely
                        Log.d("Event", "On Fling Down");
                        finish(); 
                    } else {
                        Log.d("Event", "On Fling Up");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		setMinFaceSize(size);
        return false;
        
//		float size = mRelativeFaceSize;
//		if ( velocityX < 0.0f ) // swipe forward
//		{
//			size -= 0.2f;
//			if ( size < 0.2f )
//				size = 0.2f;
//		}
//		else if ( velocityX > 0.0f ) // swipe backward
//		{
//			size += 0.2f;
//			if ( size > 0.8f )
//				size = 0.8f;
//		}
//		
//		 
//		setMinFaceSize(size);
//		
//		
//		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		Log.i(TAG, "ON PreviewFrame+++++++++++");
		
	}
	
//	
//@Override
//public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//		// TODO Auto-generated method stub
//		Log.i(TAG, "+++++++++++++++ surfaceChanged called +++++++++++++++");
//		if (bAfterInit){
//			mCameraControl.setParam();
//		}
//			
//		
//		//CameraControl mCameraControl;
//		
//		
////		if ( null != mCamera ) {
////			mCamera.stopPreview();
////			mCamera.release();
////			mCamera = null;
////		}
////		
////		mCamera = Camera.open(-1);
//		
//		//openCamera();
////		
//		if (mCamera == null) 
//			return;
////
//		Log.i(TAG, "+++++++++++++++ param setup +++++++++++++++");
//		
//		//if (!mCameraConfigured){
//	        Camera.Parameters camParameters = mCamera.getParameters();
//	        //start glass fix - use 5000 instead of 30000 for better battery performance
//	        camParameters.setPreviewFpsRange(30000, 30000);
//	        //end glass fix
//	        camParameters.setPreviewSize(1920, 1080);
//	        camParameters.setPictureSize(2592, 1944);
//	        mCamera.setParameters(camParameters);
//	        //mCameraConfigured = true;			
//	        Log.i(TAG, " surfaceChanged, done setup---------------");
//		//}
//        
//		Log.i(TAG, "----------------- surfaceChanged called ---------------");
////        try {
////            mCamera.startPreview();
////        } catch (Exception e) {
////            mCamera.release();
////            mCamera = null;
////        }
//////		
//}

//
//	@Override
//	public void surfaceCreated(SurfaceHolder holder) {
//		// TODO Auto-generated method stub
//		Log.i(TAG, "+++++++++++++++ surfaceCreated called +++++++++++++++");
//		
////		
////		Log.i(TAG, "surfaceCreated called:"+Camera.getNumberOfCameras());
////		
////		mCamera = Camera.open();
////		mCamera.setPreviewCallback(this);
//		
//	}
//
//	@Override
//	public void surfaceDestroyed(SurfaceHolder holder) {
//		// TODO Auto-generated method stub
//		
//	}



//@Override
//public void surfaceCreated(SurfaceHolder arg0) {
//	// TODO Auto-generated method stub
//	
//}
//
//@Override
//public void surfaceDestroyed(SurfaceHolder arg0) {
//	// TODO Auto-generated method stub
//	
//}
}
