package com.zwsatan.donttouchwhite;

import android.graphics.Bitmap;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;

public class WeixinShare {

	private static WeixinShare share = null;
	
	private static final String APP_ID = "wxa7c357e6c138daae";
	private static final int SMALL_WIDTH = 150;
	private static final int SMALL_HEIGHT = 260;
	
	
	private IWXAPI api;
	
	public static WeixinShare getWeixinShare() {
		if (share == null) {
			share = new WeixinShare();
		}
		
		return share;
	}
	
	private WeixinShare() {
		// 向微信注册
		api = WXAPIFactory.createWXAPI(MainActivity.getMainActivity(), APP_ID, true);
		api.registerApp(APP_ID);
	}
	
	public void sendReq(Bitmap bitmap, int scene) {

		WXImageObject imgObj = new WXImageObject(bitmap);
		
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;
		
		Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, SMALL_WIDTH, SMALL_HEIGHT, true);
		bitmap.recycle();
		msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("img");
		req.message = msg;
		req.scene = scene;
		api.sendReq(req);
	}
	
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
	
}
