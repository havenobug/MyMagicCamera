package com.example.zcamera2;
 
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2  {


    private static final String TAG = ".MainActivity";


    private Mat mRgba;
    private Mat mGray;
    private Mat mPrevGray;

    private Mat mForeground;
    private Mat kernelErode;

    private static final int RESIZE_SCALAR = 5;
    private static final byte FRAME_INTERVAL = 3;
    private static final double THRESHOLD_RATIO = 0.01;
    private static final short THRESHOLD_INTERVAL = 100;

    private int iAlertNum;
    private short iAlertInterval;


    private byte mFrameCount;
    private Boolean mIsFirstFrame;
    private double dContourArea;
    private double dMotionThreshold;

 
   private boolean isShow=false;

    private BackgroundSubtractorMOG2 mMog;

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    // System.loadLibrary("tracking");

                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
 
    private WindowManager windowManager;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);    //设置UI界面

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
       // mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.setCvCameraViewListener(this);
        getHW();
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        //mOpenCvCameraView.setWindowManager(windowManager);
         initViews();
         initClick();
         mOpenCvCameraView.setCameraIndex(cameraId);
         mOpenCvCameraView.setMaxFrameSize(680, 400);
         
    }
      private Button mTakeBtn,mFilterBtn,mFlashBtn,mChangeBtn;
	  private ImageView mPhotoImg;
	  private LinearLayout mFilterLayout;
	  private FrameLayout mFilterALayout,mFilterBLayout,mFilterCLayout,mFilterDLayout;
 	  private LinearLayout mImgLayout;// 拍照后图片预览
	     private Button mImgCancelBtn,mImgSaveBtn,mImgRetryBtn;//保存图片按钮
	     int cameraId=CameraBridgeViewBase.CAMERA_ID_FRONT;
	  
	    private int mFlashMode=CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH;
	  
		private MyImage mImgview;
    
    
		 private void initViews() {
				// 初始化控件
			     mTakeBtn=(Button) findViewById(R.id.camera_shut_btn);
			     mFilterBtn=(Button) findViewById(R.id.camera_filter_btn);
			     mFlashBtn=(Button) findViewById(R.id.camera_flash_btn);
			     mChangeBtn=(Button) findViewById(R.id.camera_backf_btn);
			     mPhotoImg=(ImageView) findViewById(R.id.camera_img);
			     mFilterLayout=(LinearLayout) findViewById(R.id.camera_fiter_layout);
			     mFilterALayout=(FrameLayout) findViewById(R.id.camera_fiter_normal_layout);
			     mFilterBLayout=(FrameLayout) findViewById(R.id.camera_fiter_his_layout);
			     mFilterCLayout=(FrameLayout) findViewById(R.id.camera_fiter_autm_layout);
			     mFilterDLayout=(FrameLayout) findViewById(R.id.camera_fiter_black_layout);
			     
			     mImgLayout=(LinearLayout) findViewById(R.id.img_layout);
			     mImgCancelBtn=(Button) findViewById(R.id.img_cancel_btn);
			     mImgSaveBtn=(Button) findViewById(R.id.img_save_btn);
			     mImgview=(MyImage) findViewById(R.id.result_img_view);
			     mMeiHuaBtn=(Button) findViewById(R.id.camera_meiyan_btn);
			     mImgview.setScreenSize(mScreenW, mScreenH);
			     mImgRetryBtn=(Button) findViewById(R.id.img_retry_btn);
			     
			     File file=new File(MyConstant.SD_PIC_PATH);
			     if (!file.exists()) {
				   file.mkdirs();
				     }
			      
			}
    
     boolean isFilter=false;
     int mFilterState=MyConstant.FILTER_STATE_NONE;


	private Mat mSepiaKernel;
 	private Mat mIntermediateMat;
     Button mMeiHuaBtn;
    boolean isMeiHua=false;
	private Size mSize0;
 	private int mScreenH;
 	private int mScreenW;
     
		 private void initClick() {
				// 交互事件
			 mImgRetryBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
				   mImgview.resetView();
				}
			});
			 mMeiHuaBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					  if (isMeiHua) {
						 isMeiHua=false;
						 mMeiHuaBtn.setBackgroundResource(R.drawable.auto_meihua_icon_person);
					}else {
						isMeiHua=true;
						 mMeiHuaBtn.setBackgroundResource(R.drawable.auto_meihua_icon_person_b);
					}
					
				}
			});
				 mTakeBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// 拍照
						  takePicture();
					}
				});
				 mFilterBtn.setOnClickListener(new OnClickListener() {
						 
						@Override
						public void onClick(View v) {
							// 滤镜
							if (isFilter) {
								 isFilter=false;
								 mFilterLayout.setVisibility(View.GONE);
							}else {
								mFilterLayout.setVisibility(View.VISIBLE);
								isFilter=true;
							  }
							
						}
					});
				/**
				 * 以下四种滤镜 
				 */
				 mFilterALayout.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						 
						mFilterState=MyConstant.FILTER_STATE_NONE;
					}
				});
				 mFilterBLayout.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
					//	 
					//	setFilter(1);
						mFilterState=MyConstant.FILTER_STATE_A;
					}
				});
				 
				 mFilterCLayout.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mFilterState=MyConstant.FILTER_STATE_B;
					 
					}
				});
				 mFilterDLayout.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {  
						//setFilter(3);
						mFilterState=MyConstant.FILTER_STATE_C;
					}
				});
				 mFlashBtn.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// 闪光灯
							    if (mFlashMode==CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH) {
							    	mFlashMode=CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH;
							    	mFlashBtn.setBackgroundResource(R.drawable.flash_on);
								}else if (mFlashMode==CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH) {
									mFlashMode=CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE;
									mFlashBtn.setBackgroundResource(R.drawable.flash_off);
								}else if (mFlashMode==CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE) {
									mFlashMode=CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH;
									mFlashBtn.setBackgroundResource(R.drawable.flash_auto);
								} 
						}
					});
				 mChangeBtn.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// 前后置摄像头
				               if (cameraId==CameraBridgeViewBase.CAMERA_ID_FRONT) {
								    cameraId=CameraBridgeViewBase.CAMERA_ID_BACK;
							  }
				               else {
								cameraId=CameraBridgeViewBase.CAMERA_ID_FRONT;
							    }
							 try {
								  
								 mOpenCvCameraView.setCameraIndex(cameraId);
							  } catch (Exception e) {
								 
								e.printStackTrace();
							  }
							
						}
					});
		   mImgSaveBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 保存照片
				 mImgview.saveBit();
				 mImgLayout.setVisibility(View.GONE);
				 
				  
			}
		});
		   
		   mImgCancelBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				  mImgLayout.setVisibility(View.GONE);
				  
			}
		});
			}
    
		  private void getHW() {
		   		DisplayMetrics ds=new DisplayMetrics();
		   		WindowManager wm = (WindowManager) this
		   				.getSystemService(Context.WINDOW_SERVICE);
		   	    wm.getDefaultDisplay().getMetrics(ds);
		   	  
		   	     mScreenH=  ((ds.heightPixels));
		   		 mScreenW= (ds.widthPixels);
		       	}  
    boolean isTakPic=false;
    String mPicPath;
    protected void takePicture() {
			// 
    	 isTakPic=true;
     	 mPicPath=MyConstant.SD_PIC_PATH + File.separator+  System.currentTimeMillis()+".jpg";
 	      mImgLayout.setVisibility(View.VISIBLE);	
        }
 

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");

        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

   
    public void onCameraViewStarted(int width, int height) {
 
    	 mSize0 = new Size();
    	mIntermediateMat=new Mat();
    	   mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
           mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
           mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
           mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
           mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();

        mForeground.release();
        
        kernelErode.release();

        mPrevGray.release();
    }
 
    public static Mat face2(Mat image) {  
        Mat dst = new Mat();  
      
        // int value1 = 3, value2 = 1; 磨皮程度与细节程度的确定  
        int value1 = 3, value2 = 1;   
        int dx = value1 * 5; // 双边滤波参数之一  
        double fc = value1 * 12.5; // 双边滤波参数之一  
        
        double p = 0.1f; // 透明度  
        Mat temp1 = new Mat(), temp2 = new Mat(), temp3 = new Mat(), temp4 = new Mat();  
         // 双边滤波  
        Imgproc.bilateralFilter(image, temp1, dx, fc, fc);  
         // temp2 = (temp1 - image + 128);  
        Mat temp22 = new Mat();  
        Core.subtract(temp1, image, temp22);  
        // Core.subtract(temp22, new Scalar(128), temp2);  
        Core.add(temp22, new Scalar(128, 128, 128, 128), temp2);  
        // 高斯模糊  
        Imgproc.GaussianBlur(temp2, temp3, new Size(2 * value2 - 1, 2 * value2 - 1), 0, 0);  
         // temp4 = image + 2 * temp3 - 255;  
        Mat temp44 = new Mat();  
        temp3.convertTo(temp44, temp3.type(), 2, -255);  
        Core.add(image, temp44, temp4);  
        // dst = (image*(100 - p) + temp4*p) / 100;  
         Core.addWeighted(image, p, temp4, 1 - p, 0.0, dst);  
         Core.add(dst, new Scalar(10, 10, 10), dst);  
        return dst;  
      
    }  
     private Mat fiter(int a,Mat mat,CameraBridgeViewBase.CvCameraViewFrame inputframe) {
		//  滤镜
    	 Mat newMat = null;
    	 
    	 Size sizeRgba = mat.size();
		    int rows = (int) sizeRgba.height;
		    int cols = (int) sizeRgba.width;
		    Mat rgbaInnerWindow;
		        
		    int left = 0*cols / 8;
		    int top = 0*rows / 8;

		    int width = cols ;
		    int height = rows ;	
    	 switch (a) {
		case MyConstant.FILTER_STATE_A:
			 
		 
			Mat gray =inputframe.gray();
            Mat grayInnerWindow = gray.submat(top, top + height, left, left + width);
            rgbaInnerWindow = mat.submat(top, top + height, left, left + width);
            
            Imgproc.Sobel(grayInnerWindow, mIntermediateMat, CvType.CV_8U, 1, 1);
            Core.convertScaleAbs(mIntermediateMat, mIntermediateMat, 10, 0);
            Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
            grayInnerWindow.release();
            rgbaInnerWindow.release();
 		     
 			    newMat= mat;
			
			break;
		case MyConstant.FILTER_STATE_B:
			
			
			
			
			
			
			
			rgbaInnerWindow = mat.submat(top, top + height, left, left + width);
            Imgproc.resize(rgbaInnerWindow, mIntermediateMat, mSize0, 0.1, 0.1, Imgproc.INTER_NEAREST);
            Imgproc.resize(mIntermediateMat, rgbaInnerWindow, rgbaInnerWindow.size(), 0., 0., Imgproc.INTER_NEAREST);
            rgbaInnerWindow.release();
 
 		    newMat=mat;

		break;
		case MyConstant.FILTER_STATE_C:
		 
				
			
		    rgbaInnerWindow = mat.submat(top, top + height, left, left + width);
            Core.transform(rgbaInnerWindow, rgbaInnerWindow, mSepiaKernel);
            rgbaInnerWindow.release();
			 
			 
		    
		newMat=mat;
		break;
		 
		}
return newMat;
	}
        private void takePic(Mat mat) {
        	  if (isTakPic) {
				  isTakPic=false;
				  Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGB2BGR);
				  Highgui.imwrite(mPicPath, mat);
				  Log.e("拍照", mPicPath);
				  runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						  Bitmap bit=  BitmapFactory.decodeFile(mPicPath);
						//  Bitmap bit=  BitmapFactory.decodeResource(getResources(), R.drawable.recent_full_panel_list_item_iv_head_camera);
						  mImgview.initView(bit,true);
						
					}
				});
			} 

		}
         Mat mResulMat;//最终 拍照 
       Random mRandom=new Random();
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
    	
    	
    	
    	   mRgba = inputFrame.rgba();  
     	  // mRgba = inputFrame.rgba();//color frame
    	   mGray = inputFrame.gray();// gray frame
    	   Core.flip(mRgba, mRgba, 1);//flip aroud Y-axis
    	   Core.flip(mGray, mGray, 1);
    	   
                if (isMeiHua){
  				  Imgproc.cvtColor(mRgba,mRgba,Imgproc.COLOR_BGR2RGB);
  		   	      Mat src3 = face2(mRgba);  
  		    	  Imgproc.cvtColor(src3,src3,Imgproc.COLOR_RGB2BGR);
  		    	   
  		    	   if (mFilterState!=MyConstant.FILTER_STATE_NONE) {
  		    		       mResulMat=fiter(mFilterState,src3,inputFrame);
  		    		       takePic(mResulMat);
  		    		       return mResulMat;
				    }else {
				    	
				    	
				    	  mResulMat=src3;
				    	  takePic(mResulMat);
 				    	 return src3;
					}
  		    	 
				} else {
					if (mFilterState!=MyConstant.FILTER_STATE_NONE) {
						  mResulMat=fiter(mFilterState,mRgba,inputFrame);
						  takePic(mResulMat);
			    		  return  mResulMat;
					   }
				}

    	 
                takePic(mRgba);
		    	   
        return mRgba;
     
    }

    private void resetVars() {
        mPrevGray = new Mat(mGray.rows(), mGray.cols(), CvType.CV_8UC1);
    }

    /**Bitmap转base64编码
     * bitmapתΪbase64
     *
     * @param bitmap
     * @return String
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

 

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
//    public Action getIndexApiAction() {
//        Thing object = new Thing.Builder()
//                .setName("Main Page") // TODO: Define a title for the content shown.
//                // TODO: Make sure this auto-generated URL is correct.
//                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
//                .build();
//        return new Action.Builder(Action.TYPE_VIEW)
//                .setObject(object)
//                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
//                .build();
//    }

    @Override
    public void onStart() {
        super.onStart();

//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        AppIndex.AppIndexApi.end(client, getIndexApiAction());
//        client.disconnect();
    }

 
   
}
