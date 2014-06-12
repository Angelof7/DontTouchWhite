package com.zwsatan.donttouchwhite;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.view.View;

public class Util {
	/**
	* 获取和保存当前屏幕的截图
	*/
	public static Bitmap shareScreen(Activity activity)  
	{  
		// 构建Bitmap  
		Bitmap bitmap = Bitmap.createBitmap(MainActivity.SCREEN_WIDTH, MainActivity.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888); 
		
		// 获取屏幕  
		View decorview = activity.getWindow().getDecorView();   
		decorview.setDrawingCacheEnabled(true);
		decorview.buildDrawingCache(); 
		bitmap = decorview.getDrawingCache();  
		
		return bitmap;
	}
	
	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}
		
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
