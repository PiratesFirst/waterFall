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
	private final int NUM_OF_PIC = 1; //ÿ�μ��ص�ͼƬ������ÿ�������ͼƬ��������: NUM_OF_PIC * LOAD_TIMES_PER_REQUEST��
	private final int LOAD_TIMES_PER_REQUEST = 1000;//ÿ������ļ��ش���
    static LinearLayout linearLayout1 ;LinearLayout linearLayout2;LinearLayout linearLayout3;

    public static View scrollView = null;
    private int USE_LINEAR_INTERVAL = 0;
    public static int linearlayoutWidth = 0;//���ڻ����Ļ����֮һ�Ŀ��
    private byte[][] picBytes = new byte[NUM_OF_PIC][];//��ȡ��ͼƬbyte��������
    public static int numOfPic = 0;
    private int index = 0; 
    public static int requestTime = 0;   //����Ĵ���;
    private PictureArrayOfBytes picArray ;
    private View view;
    private ImageView imageView;
    public static boolean imageIsAdd = true;   //��ʶÿ�γɹ�����ͼƬ
    public static int imageIsAddTimes = 0;  //���ص�ͼƬ����
    public static boolean againLoadImageSigal = false;  //�ٴ��������ͼƬ�ı�ʶ
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
        
        Log.e("error","������һ���߳�");
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
                    	Toast.makeText(getApplicationContext(), "�������ײ���",
                    		     Toast.LENGTH_SHORT).show();
                    	if ( againLoadImageSigal == true){//ǰ���LOAD_TIMES_PER_REQUEST�λ�ȡ��ͼƬ����ȫ�����꣬�����ø��źţ���ʶ�����ٴ������ȡͼƬ��ͼƬδ�����꣬��������Ч
                    		Log.e("error","�ٴ������ȡͼƬ�ɹ�");
                    		imageIsAddTimes = 0;//��ʼ�����ش�����ÿ��������Զ�����LOAD_TIMES_PER_REQUEST��ͼƬ
                    		imageIsAdd = true;//imageIsAdd����Ϊture���̻߳��ø��źţ���ʼ�ӷ�����ץȥͼƬ
                    		againLoadImageSigal = false;//��ǰһ�������ͼƬδ������֮ǰ���޷��ٴ��������ͼƬ
                    	}else{
                    		Log.e("error","�޷��������ͼƬ");
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

	
	/**ͨ����õ�ͼƬ���ݣ���ͼƬ���ص������У�ͬʱ��ɾ����Ļ���沿�������صĲ���ͼƬ*/
	private void downAddBitmaps(byte[][] imageData){
		for (int i = 0; i < NUM_OF_PIC; i++){
			Bitmap bitmap = BitmapFactory.decodeByteArray(picBytes[i], 0, picBytes[i].length); //��byteͼƬ���ݣ�ת����BitmapͼƬ
			Bitmap bitmap2 = BitmapZoom.bitmapZoomByWidth(bitmap, linearlayoutWidth); //����linearlayoutWidth��ֵѹ��ͼƬ�Ĵ�С
			heightOfImage[i%3] += bitmap2.getHeight();    							// ��¼�������Բ��ֵĸ߶�
			imageView = new ImageView(getContext());
			imageView.setImageBitmap(bitmap2);										//��imageView������ͼƬ��ϵ��һ��
			//����ͼƬ�������ֵĳ��Ϳ�layoutParams������ͼƬ������װ��ͼƬ����������С���ܱ�ͼƬС
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(bitmap2.getWidth()+20, bitmap2.getHeight()+20);
			//��ͼƬ������imageView��ϵ��һ��
			imageView.setLayoutParams(layoutParams);
			switch (USE_LINEAR_INTERVAL) 
			{
				case 0:
					linearLayout1.addView(imageView);								//���ش������imageView����
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
		
		/**������һ�����Բ��ּ��ص�imageView����ﵽ20������ʼ����ɾ��ǰ10������*/
		if (linearLayout1ImageNum >= 30+10 || linearLayout2ImageNum >= 30+10 || linearLayout3ImageNum >= 30+10){
			linearLayout1.removeViews(0,1);
			linearLayout2.removeViews(0,1);
			linearLayout3.removeViews(0,1);
            System.gc(); //ɾ��view�����ִ����������
            linearLayout1ImageNum = linearLayout1ImageNum - 1;  //��ȥɾ��view������
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
          		    	 Log.e("error","��ʼ����ͼƬ");
          		    	 downAddBitmaps(picBytes);
          			     imageIsAddTimes++;//������صĴ�����ÿ�������ͼƬ������ɺ󣬻�����Ϊ0;
          			     Log.e("error","����ͼƬ�Ĵ���"+ imageIsAddTimes);
          			     if (imageIsAddTimes < LOAD_TIMES_PER_REQUEST){   /**����ͼƬ�󣬲�������ץȥͼƬ��û�д��ж���䣬���������һ��ͼƬ�����غ�
          			     													imageIsAdd�ᱻ����Ϊtrue�������̻߳��ٴδӷ�����ץȥͼƬ�����ǲ���
          			     													�����أ����´˴�ץȥ��ͼƬ��ʧ*/
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
					Log.e("error","��ʼ�ӷ�������ȡͼƬ���߳�");
					picArray = new PictureArrayOfBytes(++requestTime);
					picBytes = picArray.getPicByteArray();
					Message message = Message.obtain();  
					message.what = 0;
					Log.e("error","�߳�ִ�����,");
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
