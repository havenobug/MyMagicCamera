package com.example.zcamera2;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.media.ExifInterface;
import android.os.Environment;

  
public class MyConstant {

	public static final int FRONT_PAGE = 0X5001;
	public static final long DELAYED = 2500;//欢迎界面持续时间
	public static final int FRESH_HIS = 0x5002;//刷新历史数据
	public static final int NOTIFY_ADPTER = 0X5003;//适配器更新
 	public static final long THREAD_SLEEP = 50;//线程休息50毫秒 
	public static final String	TAG_DATETIME=ExifInterface.TAG_DATETIME;//时间日期 
 
 	public static final String TAG_IMAGE_LENGTH=ExifInterface.TAG_IMAGE_LENGTH;//　图片长 
	public static final String TAG_IMAGE_WIDTH=ExifInterface.TAG_IMAGE_WIDTH;//图片宽 
	public static final String TAG_MAKE=ExifInterface.TAG_MAKE;//　设备制造商 
	public static final String TAG_MODEL=ExifInterface.TAG_MODEL;//设备型号 
	public static final String TAG_ORIENTATION=ExifInterface.TAG_ORIENTATION;//方向
  
 
	
	 
    public static final String IMG_SDPATH=Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"DCIM"
    		+File.separator+"Camera";// 相机照片位置
    //public static final String IMG_CAMERA_PATH= (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+"MyDICM");

	public static final String IMG_KEY = "IMG_KEY";// 图片地址传递key
	public static final String IMG_NAME = "IMG_NAME";
	public static final int REQUEST_CAMERA = 5;//调用相机请求码
	public static final int TYPE_TAKE_PHOTO = 1;//拍照
 
 	public static final String IMG_FULL_KEY = "IMGFULL_KEY";//全屏key
	public static final int REQUEST_INFO = 6;// info请求码
	public static final int RESULT_INFO_CODE = 6;//info 返回码
	public static final int CAMERA_ID_FRONT =1 ;//前置摄像头
	public static final int CAMERA_ID_BACK =0 ;//置摄像头
	public static final String SD_PIC_PATH=Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"MyDCIM";//相册名称
	public static final int REQUEST_CAMERA_KODE = 9;//开启相机 请求码
	public static final int ROTATE_NOR = 0;//旋转 不是90度
	public static final int ROTATE_FENOR = 1;
	
   public static final int FILTER_STATE_NONE=0,FILTER_STATE_A=3,FILTER_STATE_B=2,FILTER_STATE_C=1;
   
	
	
	public static String TimeDateFormat(long time) {
		//  获取特定时间格式
		  String s="null";
          SimpleDateFormat formate=new SimpleDateFormat("yyyy:MM:dd:");
          s=formate.format(new Date(time));
          
		  return s;
	}
	  
	  public static String TimeMouthFormat(long time) {
			//  获取特定时间格式 获取到月
			  String s="null";
	          SimpleDateFormat formate=new SimpleDateFormat("yyyy年MM月");
	          s=formate.format(new Date(time));
	          
			  return s;
		}
	  
	  public static String TimeDayFormat(long time) {
			//  获取特定时间格式 获取到日
			  String s="null";
	          SimpleDateFormat formate=new SimpleDateFormat("MM月dd日");
	          s=formate.format(new Date(time));
	          
			  return s;
		}
	  public static String TimeHourFormat(long time) {
			//  获取特定时间格式
			  String s="null";
	          SimpleDateFormat formate=new SimpleDateFormat("yy/MM/dd HH:mm");
	          s=formate.format(new Date(time));
	          
			  
			  return s;
		}
}
