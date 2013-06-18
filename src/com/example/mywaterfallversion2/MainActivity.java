package com.example.mywaterfallversion2;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static Context mContext;  
	private final int NUM_OF_PIC = 1; //每次加载的图片数量（每次申请的图片总数量是: NUM_OF_PIC * LOAD_TIMES_PER_REQUEST）
	private final int LOAD_TIMES_PER_REQUEST = 1000;//每次申请的加载次数
    static LinearLayout linearLayout1 ;LinearLayout linearLayout2;LinearLayout linearLayout3;

    public static View scrollView = null;
    private int USE_LINEAR_INTERVAL = 0;
    public static int linearlayoutWidth = 0;//用于获得屏幕三分之一的宽度
    private byte[][] picBytes = new byte[NUM_OF_PIC][];//获取的图片byte数据数组
    public static int numOfPic = 0;
    private int index = 0; 
    public static int requestTime = 0;   //申请的次数;
    private PictureArrayOfBytes picArray ;
    private View view;
    private ImageView imageView;
    public static boolean imageIsAdd = true;   //标识每次成功加载图片
    public static int imageIsAddTimes = 0;  //加载的图片次数
    public static boolean againLoadImageSigal = false;  //再次申请加载图片的标识
    private static int linearLayout1ImageNum = 0;
    private static int linearLayout2ImageNum = 0;
    private static int linearLayout3ImageNum = 0;
    private static int[] heightOfImage = new int[3];

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mContext = getApplicationContext();
        linearLayout1 = (LinearLayout)findViewById(R.id.main_linearlayout1);
        linearLayout2 = (LinearLayout)findViewById(R.id.main_linearlayout2);
        linearLayout3 = (LinearLayout)findViewById(R.id.main_linearlayout3);
        linearlayoutWidth =  (int)(getWindowManager().getDefaultDisplay().getWidth()/3);
        scrollView = findViewById(R.id.scroll);
        
        Log.e("error","开启第一个线程");
        thread.start();
        scrollView.setOnTouchListener(new OnTouchListener() {  
            @Override  
            public boolean onTouch(View v, MotionEvent event) {  
                switch (event.getAction()) {  
                    case MotionEvent.ACTION_DOWN :  
                        break;  
                    case MotionEvent.ACTION_MOVE :  
                        index++;  
                        break;  
                    default :  
                        break;  
                }  
                if (event.getAction() == MotionEvent.ACTION_UP &&  index > 0) {  
                    index = 0;  
                    view = ((ScrollView) v).getChildAt(0);  
                    if (view.getMeasuredHeight() <= v.getScrollY() + v.getHeight()) {  
                    	Toast.makeText(getApplicationContext(), "滚动到底测试",
                    		     Toast.LENGTH_SHORT).show();
                    	if ( againLoadImageSigal == true){//前面的LOAD_TIMES_PER_REQUEST次获取的图片已完全加载完，则设置该信号，标识可以再次申请获取图片。图片未加载完，则申请无效
                    		Log.e("error","再次申请获取图片成功");
                    		imageIsAddTimes = 0;//初始化加载次数，每次申请会自动加载LOAD_TIMES_PER_REQUEST次图片
                    		imageIsAdd = true;//imageIsAdd设置为ture，线程会获得该信号，开始从服务器抓去图片
                    		againLoadImageSigal = false;//在前一次申请的图片未加载完之前，无法再次申请加载图片
                    	}else{
                    		Log.e("error","无法申请加载图片");
                    	}
                    }  
                }  
                return false;  
            }  
        });  
		
	}
    public static Context getContext(){  
        return mContext;  
    } 

	
	/**通过获得的图片数据，将图片加载到布局中，同时会删除屏幕上面部分已隐藏的部分图片*/
	private void downAddBitmaps(byte[][] imageData){
		for (int i = 0; i < NUM_OF_PIC; i++){
			Bitmap bitmap = BitmapFactory.decodeByteArray(picBytes[i], 0, picBytes[i].length); //将byte图片数据，转换成Bitmap图片
			Bitmap bitmap2 = BitmapZoom.bitmapZoomByWidth(bitmap, linearlayoutWidth); //根据linearlayoutWidth的值压缩图片的大小
			heightOfImage[i%3] += bitmap2.getHeight();    							// 记录三个线性布局的高度
			imageView = new ImageView(getContext());
			imageView.setImageBitmap(bitmap2);										//将imageView对象与图片联系在一起
			//设置图片容器布局的长和宽，layoutParams并不是图片，而是装载图片的容器，大小不能比图片小
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(bitmap2.getWidth()+20, bitmap2.getHeight()+20);
			//将图片容器与imageView联系在一起
			imageView.setLayoutParams(layoutParams);
			switch (USE_LINEAR_INTERVAL) 
			{
				case 0:
					linearLayout1.addView(imageView);								//加载处理过的imageView对象
					linearLayout1ImageNum++;
					break;
				case 1:
					linearLayout2.addView(imageView);
					linearLayout2ImageNum++;
					break;
				case 2:
					linearLayout3.addView(imageView);
					linearLayout3ImageNum++;
					break;
				default:
					break;
			}
			USE_LINEAR_INTERVAL++;
			USE_LINEAR_INTERVAL= USE_LINEAR_INTERVAL%3;
		}
		
		/**当其中一个线性布局加载的imageView对象达到20个，则开始启动删除前10个对象*/
		if (linearLayout1ImageNum >= 30+10 || linearLayout2ImageNum >= 30+10 || linearLayout3ImageNum >= 30+10){
			linearLayout1.removeViews(0,1);
			linearLayout2.removeViews(0,1);
			linearLayout3.removeViews(0,1);
            System.gc(); //删除view对象后执行垃圾回收
            linearLayout1ImageNum = linearLayout1ImageNum - 1;  //减去删除view的数量
            linearLayout2ImageNum = linearLayout2ImageNum - 1;
            linearLayout3ImageNum = linearLayout3ImageNum - 1;
        
		}
	}
	
    private Handler mHandler = new Handler(){  
        public void handleMessage(Message msg) {  
            switch (msg.what) {  
            case 0:   
          	  try {
          		      if(imageIsAddTimes < LOAD_TIMES_PER_REQUEST){
          		    	 Log.e("error","开始加载图片");
          		    	 downAddBitmaps(picBytes);
          			     imageIsAddTimes++;//计算加载的次数，每次申请的图片加载完成后，会重置为0;
          			     Log.e("error","加载图片的次数"+ imageIsAddTimes);
          			     if (imageIsAddTimes < LOAD_TIMES_PER_REQUEST){   /**加载图片后，不在申请抓去图片，没有此判断语句，将导致最后一次图片被加载后，
          			     													imageIsAdd会被设置为true，导致线程会再次从服务器抓去图片，但是不会
          			     													被加载，导致此次抓去的图片丢失*/
          			    	imageIsAdd = true;
          			     }else{
          			    	againLoadImageSigal = true;
          			     }
          		      }
          		      	      
  			} catch (Exception e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			}
                break;  

  			default :
					break;
					
            }  
        } 
    };
    Runnable Images = new Runnable(){
	public void run() {
		// TODO Auto-generated method stub
		try {	
			
			while(true){
				if(imageIsAdd == true){
					picBytes = null;
					Log.e("error","开始从服务器获取图片的线程");
					picArray = new PictureArrayOfBytes(++requestTime);
					picBytes = picArray.getPicByteArray();
					Message message = Message.obtain();  
					message.what = 0;
					Log.e("error","线程执行完毕,");
					mHandler.sendMessage(message);
					imageIsAdd = false;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	};
	Thread thread = new Thread(Images);
}
