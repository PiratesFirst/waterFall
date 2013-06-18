package com.example.mywaterfallversion2;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class PictureArrayOfBytes {
	private int request = 0;  	//请求获取图片的次数
	private final int NUM_OF_PIC = 1;  //每次请求获取的图片数量
	
	//json数据的前缀地址
	private final String MAIN_URL = "http://itest.51ishare.com/json_brand/auth_recommend_order/";
	//每张图片的前缀地址
	private final String MainUrl = "http://itest.51ishare.com/";

	
	//初始化
	public PictureArrayOfBytes(int request){
		this.request = request;
	}
	
	//获取图片数据数组
	public byte[][] getPicByteArray() throws Exception{
		String tempUrl = getUrl();
		String tempJsonData = getJsonData(tempUrl);
		ArrayList<pictureAttr> tempAttrs = parseJsonMulti(tempJsonData);
		String[] tempPcitureUrl = getPictureUrl(tempAttrs);
		byte[][] PicByteArray = new byte[NUM_OF_PIC][];
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		for (int j = 0; j < NUM_OF_PIC; j++){
			HttpGet request = new HttpGet(tempPcitureUrl[j]);
			HttpResponse response = httpClient.execute(request);
			PicByteArray[j] = EntityUtils.toByteArray(response.getEntity());
		}
		Log.e("error","获得图片数据");
		return PicByteArray;
	}
	
	//获取json数据的网络地址
	private String getUrl (){
		return MAIN_URL + request + "/" + NUM_OF_PIC;
	}
	
	//根据网址url获得json格式的数据
	public String getJsonData(String url) {
		try {
			String result = null; 
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			HttpResponse response = httpClient.execute(request);
			result = EntityUtils.toString(response.getEntity());
			return result;
              
		} catch (Exception e) {
  	// TODO: handle exception
		}
		return null;
	}
	
	//根据json格式的数据，解析并获取图片数组的属性
	private ArrayList<pictureAttr> parseJsonMulti(String result){
		//获得每张图片的属性(属性可以计算出图片的绝对网络地址，图片的高，图片的宽)
		List<pictureAttr> attrsOfAllPictures = new ArrayList<pictureAttr>();
		try { 
			JSONArray jsonObjs = new JSONObject(result).getJSONArray("data"); 
			for(int i = 0; i < jsonObjs.length() ; i++){ 
				JSONObject jsonObj = ((JSONObject)jsonObjs.opt(i));
				String subURL = jsonObj.getString("bp_thumb");
				int pic_width = jsonObj.getInt("pic_width"); 
				int pic_height = jsonObj.getInt("pic_height");
			 
				pictureAttr item = new pictureAttr();
			 
				item.setPicHeight(pic_height);
				item.setPicWidth(pic_width);
				item.setSubUrl(subURL);
			 
				attrsOfAllPictures.add(item);
			}
			return (ArrayList<pictureAttr>) attrsOfAllPictures;
		} catch (JSONException e) { 
			System.out.println("Jsons parse error !");
			e.printStackTrace();
		}
		return null;
	}
	
	//根据属性列表获取每张图片的地址
	private String[] getPictureUrl(List<pictureAttr> attrsOfAllPictures){
		String[] picUrl = new String[NUM_OF_PIC];
		for (int i = 0; i < attrsOfAllPictures.size(); i++){
			picUrl[i] = MainUrl + attrsOfAllPictures.get(i).getSubUrl();
		}
		return picUrl;
	}

}

//pictureAttr类存储每张图片的属性
class pictureAttr {
	private int pic_width = 0;
	private int pic_height = 0;
	private String subUrl = null;
	
	public void setPicWidth(int pic_width){
		this.pic_width = pic_width;
	}
	
	public void setPicHeight(int pic_height){
		this.pic_height = pic_height;
	}
	
	public void setSubUrl(String subUrl){
		this.subUrl = subUrl;
	}
	
	public int getPicWidth(){
		return pic_width;
	}
	
	public int getPicHeight(){
		return pic_height;
	}
	
	public String getSubUrl(){
		return subUrl;
	}
}


