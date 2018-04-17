package com.example.zcamera2;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

public class MyImage extends View {

    private int screenWidth, screenHeight;//屏幕的宽高

    private int mWidth, mHeight;//View 的宽高

    //作用范围半径
    private int r = 100;
     private Paint circlePaint;
   

    //是否显示变形圆圈
    private boolean showCircle;
    

    //变形起始坐标,滑动坐标
    private float startX, startY, moveX, moveY;

    //将图像分成多少格
    private int WIDTH = 200;
    private int HEIGHT = 200;

    //交点坐标的个数
    private int COUNT = (WIDTH + 1) * (HEIGHT + 1);

    //用于保存COUNT的坐标
    //x0, y0, x1, y1......
    private float[] verts = new float[COUNT * 2];

    //用于保存原始的坐标
    private float[] orig = new float[COUNT * 2];

    private Bitmap mBitmap;

    
   private Bitmap mSetBit;
    public MyImage(Context context) {
        super(context);
        
        init();
    }

    public MyImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(5);
        circlePaint.setColor(Color.argb(66, 200, 20, 30));

        
    }
  boolean isStartDraw=false;
    public void initView(  Bitmap bit,boolean isStart ) {
    	isStartDraw=false;
        int index = 0;
        mSetBit=bit;
         mBitmap = zoomBitmap(mSetBit, mWidth, mHeight);
        float bmWidth = mBitmap.getWidth();
        float bmHeight = mBitmap.getHeight();

        for (int i = 0; i < HEIGHT + 1; i++) {
            float fy = bmHeight * i / HEIGHT;
            for (int j = 0; j < WIDTH + 1; j++) {
                float fx = bmWidth * j / WIDTH;
                //X轴坐标 放在偶数位
                verts[index * 2] = fx;
                orig[index * 2] = verts[index * 2];
                //Y轴坐标 放在奇数位
                verts[index * 2 + 1] = fy;
                orig[index * 2 + 1] = verts[index * 2 + 1];
                index += 1;
            }
        }
        isStartDraw=isStart;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
         
    }

    private Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        float scale = Math.min(scaleWidth,scaleHeight);
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isStartDraw) {
        	 canvas.drawBitmapMesh(mBitmap, WIDTH, HEIGHT, verts, 0, null, 0, null);
             if (showCircle) {
                canvas.drawCircle(startX, startY, r, circlePaint);
               }
		}
        
        } 
    
    
    public void saveBit() {
		//  保存
    	  Bitmap bit=Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Config.ARGB_8888);
    	 Canvas ca=new Canvas(bit);
    	 ca.drawBitmapMesh(mBitmap, WIDTH, HEIGHT, verts, 0, null, 0, null);
    	 SimpleDateFormat forma=new SimpleDateFormat("yyyyMMddHHmmss");  
	        Date   curDate   =   new   Date(System.currentTimeMillis()); 
	        String   str   =   forma.format(curDate);  
	 
	         str = str + ".jpg";
	         File dir = new File(MyConstant.SD_PIC_PATH);
	         if (!dir.exists()) { 
		            dir.mkdir(); 
		            } 
	         File file = new File(MyConstant.SD_PIC_PATH,str);
	        
	      
	         
	         
	        try {
	            FileOutputStream out = new FileOutputStream(file);
	             bit.compress(Bitmap.CompressFormat.JPEG, 100, out); 
	            out.flush(); 
	            out.close(); 
	 
	             
	     
	        } catch (FileNotFoundException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } 
	         
	}
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //绘制变形区域
                startX = event.getX();
                startY = event.getY();
                showCircle = true;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                //绘制变形方向
                moveX = event.getX();
                moveY = event.getY();
                
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                showCircle = false;
               
           if (isStartDraw) {
        	   //调用warp方法根据触摸屏事件的坐标点来扭曲verts数组
               warp(startX, startY, event.getX(), event.getY());
		}
               

             
                break;
        }
        return true;
    }

    private void warp(float startX, float startY, float endX, float endY) {

        //计算拖动距离
        float ddPull = (endX - startX) * (endX - startX) + (endY - startY) * (endY - startY);
        float dPull = (float) Math.sqrt(ddPull);
       
      //  dPull = screenWidth - dPull >= 0.0001f ? screenWidth - dPull : 0.0001f;

        for (int i = 0; i < COUNT * 2; i += 2) {
            //计算每个坐标点与触摸点之间的距离
            float dx = verts[i] - startX;
            float dy = verts[i + 1] - startY;
            float dd = dx * dx + dy * dy;
            float d = (float) Math.sqrt(dd);
            
             if (d < r) {
                //变形系数，扭曲度
                double e = (r * r - dd) * (r * r - dd) / ((r * r - dd + dPull * dPull) * (r * r - dd + dPull * dPull));
                double pullX = e * (endX - startX)/2;
                double pullY = e * (endY - startY)/2;
                verts[i] = (float) (verts[i] + pullX);
                verts[i + 1] = (float) (verts[i + 1] + pullY);
               }
        }

        invalidate();
    }

     
    public void resetView() {
        for (int i = 0; i < verts.length; i++) {
            verts[i] = orig[i];
        }
        
        invalidate();
    }

    public void setScreenSize(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

     
}
