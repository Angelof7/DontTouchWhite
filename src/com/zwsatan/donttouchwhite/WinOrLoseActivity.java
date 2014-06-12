package com.zwsatan.donttouchwhite;

import com.zwsatan.donttouchwhite.GameView.GameMode;
import com.zwsatan.donttouchwhite.GameView.GameState;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WinOrLoseActivity extends Activity {

	GameRecord gameRecord;
	
	private LinearLayout mainLayout;
	private Button buttonRestart;
	private Button buttonShare;
	private Button buttonBack;
	private TextView textGameMode;
	private TextView textNewRecordTitle;
	private TextView textFailTitle;
	private TextView textRecord;
	private TextView textBestRecord;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//全屏    
		requestWindowFeature(Window.FEATURE_NO_TITLE);    
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
		
		setContentView(R.layout.win_lose);
		
		gameRecord = GameRecordKeeper.readGameRecord(this);
		
		Intent intent = getIntent();
		final GameMode gameMode = (GameMode) intent.getSerializableExtra("GameMode");
		final GameState gameState = (GameState) intent.getSerializableExtra("GameState");
		
		
		mainLayout = (LinearLayout) findViewById(R.id.win_lose_dialog);
		textGameMode = (TextView) findViewById(R.id.text_game_mode);
		textNewRecordTitle = (TextView) findViewById(R.id.text_new_record_title);
		textFailTitle = (TextView) findViewById(R.id.text_fail_title);
		textRecord = (TextView) findViewById(R.id.text_record);
		textBestRecord = (TextView) findViewById(R.id.text_best__record);
		
		buttonRestart = (Button) findViewById(R.id.button_restart);
		buttonRestart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(WinOrLoseActivity.this, MainActivity.class);
				intent.putExtra("RestartGame", true);
				intent.putExtra("GameMode", gameMode);
				startActivity(intent);
			}
		});
		
		// 分享按钮暂时没做
		buttonShare = (Button) findViewById(R.id.button_share);
		buttonShare.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				Toast.makeText(WinOrLoseActivity.this, "Comming soon...", Toast.LENGTH_SHORT).show();
			}
		});

		buttonBack = (Button) findViewById(R.id.button_back);
		buttonBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				startActivity(new Intent(WinOrLoseActivity.this, MainActivity.class));
			}
		});
		
		if (gameMode == GameMode.GAME_CLASSIC) {
			// 事实上，只有此模式下，点击会出现失败的现象
			textGameMode.setText("经典模式");
			
			float timeRecord = intent.getFloatExtra("TimeRecord", 0f);
			
			if (gameState == GameState.GAME_WIN) {
				if (timeRecord < gameRecord.classicRecord) {
					// 记录最佳时间
					textRecord.setText(timeRecord + "\"");
					gameRecord.classicRecord = timeRecord;
					showNewRecord();
				} else {
					textRecord.setText(timeRecord + "\"");
					textBestRecord.setText("骄人战绩：" + gameRecord.classicRecord + "\"");
					showNormalWin();
				}
			} else if (gameState == GameState.GAME_OVER) {
				showLose();
			}
		} else if (gameMode == GameMode.GAME_FASTER) {
			textGameMode.setText("街机模式");
			
			int blockRecord = intent.getIntExtra("BlockRecord", 0);
			if (blockRecord > gameRecord.fasterRecord) {
				textRecord.setText(blockRecord + "");
				gameRecord.fasterRecord = blockRecord;
				showNewRecord();
			} else {
				textRecord.setText(blockRecord + "");
				textBestRecord.setText("骄人战绩：" + gameRecord.fasterRecord + "");
				showNormalWin();
			}
		} else if (gameMode == GameMode.GAME_Zen) {
			textGameMode.setText("禅模式");

			int blockRecord = intent.getIntExtra("BlockRecord", 0);
			if (blockRecord > gameRecord.zenRecord) {
				textRecord.setText(blockRecord + "");
				gameRecord.zenRecord = blockRecord;
				showNewRecord();
			} else {
				textRecord.setText(blockRecord + "");
				textBestRecord.setText("骄人战绩：" + gameRecord.zenRecord + "");
				showNormalWin();
			}
		}

	}
	
	private void showNormalWin() {
		mainLayout.setBackgroundColor(getResources().getColor(R.color.normallack));
		buttonShare.setBackgroundColor(getResources().getColor(R.color.normallack));
		buttonBack.setBackgroundColor(getResources().getColor(R.color.normallack));
		
		textNewRecordTitle.setVisibility(TextView.GONE);
		textFailTitle.setVisibility(TextView.GONE);
		textRecord.setVisibility(TextView.VISIBLE);
		textBestRecord.setVisibility(TextView.VISIBLE);
	}
	
	private void showNewRecord() {
		SoundEngine.getSoundEngine().playWinSound();
		
		mainLayout.setBackgroundColor(getResources().getColor(R.color.nicegreen));
		buttonShare.setBackgroundColor(getResources().getColor(R.color.nicegreen));
		buttonBack.setBackgroundColor(getResources().getColor(R.color.nicegreen));
		
		textNewRecordTitle.setVisibility(TextView.VISIBLE);
		textFailTitle.setVisibility(TextView.GONE);
		textRecord.setVisibility(TextView.VISIBLE);
		textBestRecord.setVisibility(TextView.GONE);
	}
	
	private void showLose() {
		mainLayout.setBackgroundColor(getResources().getColor(R.color.loserred));
		buttonShare.setBackgroundColor(getResources().getColor(R.color.loserred));
		buttonBack.setBackgroundColor(getResources().getColor(R.color.loserred));

		textNewRecordTitle.setVisibility(TextView.GONE);
		textFailTitle.setVisibility(TextView.VISIBLE);
		textRecord.setVisibility(TextView.GONE);
		textBestRecord.setVisibility(TextView.GONE);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		GameRecordKeeper.writeGameRecord(this, gameRecord);
	}
}
