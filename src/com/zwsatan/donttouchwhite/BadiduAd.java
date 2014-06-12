package com.zwsatan.donttouchwhite;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.baidu.mobads.InterstitialAd;
import com.baidu.mobads.InterstitialAdListener;

public class BadiduAd {
	
	public static BadiduAd getBadiduAd() {
		if (ad == null) {
			ad = new BadiduAd(MainActivity.getMainActivity());
		}
		
		return ad;
	}
	
	private BadiduAd(Context context) {
		
		interAd = new InterstitialAd(context);
		interAd.setListener(new InterstitialAdListener(){

			@Override
			public void onAdClick(InterstitialAd arg0) {
				Log.i("InterstitialAd","onAdClick");
			}

			@Override
			public void onAdDismissed() {
				Log.i("InterstitialAd","onAdDismissed");
				interAd.loadAd();
			}

			@Override
			public void onAdFailed(String arg0) {
				Log.i("InterstitialAd","onAdFailed");
			}

			@Override
			public void onAdPresent() {
				Log.i("InterstitialAd","onAdPresent");
			}

			@Override
			public void onAdReady() {
				Log.i("InterstitialAd","onAdReady");
			}
			
		});
		interAd.loadAd();
	}
	
	public void showAd(Activity activity) {
		if(interAd.isAdReady()){
			interAd.showAd(activity);
		}else{
			interAd.loadAd();
		}
	}

	private static BadiduAd ad = null;
	InterstitialAd interAd;
}
