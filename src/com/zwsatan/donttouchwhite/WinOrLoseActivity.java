package com.zwsatan.donttouchwhite;

import com.zwsatan.donttouchwhite.GameView.GameMode;
import com.zwsatan.donttouchwhite.GameView.GameState;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WinOrLoseActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.win_lose);
		
		initGameStatus();
		initUI();
		initButton();
		
		handleGameMode();
		
		BadiduAd.getBadiduAd(this).showAd(this);
	}
	
	/**
	 * 屏蔽掉返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * 获取从GameView传来的数据
	 */
	private void initGameStatus() {
		gameRecord = GameRecordKeeper.readGameRecord(this);
		
		Intent intent = getIntent();
		gameMode = (GameMode) intent.getSerializableExtra("GameMode");
		gameState = (GameState) intent.getSerializableExtra("GameState");
		
		timeRecord = intent.getFloatExtra("TimeRecord", 9999f);
		blockRecord = intent.getIntExtra("BlockRecord", 0);
	}
	
	/**
	 * 初始化界面UI handler
	 */
	private void initUI() {
		mainLayout = (LinearLayout) findViewById(R.id.win_lose_dialog);
		textGameMode = (TextView) findViewById(R.id.text_game_mode);
		textNewRecordTitle = (TextView) findViewById(R.id.text_new_record_title);
		textFailTitle = (TextView) findViewById(R.id.text_fail_title);
		textRecord = (TextView) findViewById(R.id.text_record);
		textBestRecord = (TextView) findViewById(R.id.text_best__record);
	}
	
	/**
	 * 初始化界面按钮及监听器
	 */
	private void initButton() {
		buttonRestart = (Button) findViewById(R.id.button_restart);
		buttonRestart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(WinOrLoseActivity.this, MainActivity.class);
				intent.putExtra(DATA_IS_GAME_RESTART, true);
				intent.putExtra(DATA_GAME_MODE, gameMode);
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
	}

	private void handleGameMode() {
		if (gameMode == GameMode.GAME_CLASSIC) {
			showClassic();
		} else if (gameMode == GameMode.GAME_FASTER) {
			showFaster();
		} else if (gameMode == GameMode.GAME_Zen) {
			showZen();
		}
	}
	
	private void showNormalWin(String record, String bestRecord) {
		mainLayout.setBackgroundColor(getResources().getColor(R.color.normallack));
		buttonShare.setBackgroundColor(getResources().getColor(R.color.normallack));
		buttonBack.setBackgroundColor(getResources().getColor(R.color.normallack));
		
		textNewRecordTitle.setVisibility(TextView.GONE);
		textFailTitle.setVisibility(TextView.GONE);
		textRecord.setVisibility(TextView.VISIBLE);
		textRecord.setText(record);
		textBestRecord.setVisibility(TextView.VISIBLE);
		textBestRecord.setText("骄人战绩：" + bestRecord);
	}
	
	private void showNewRecord(String newRecord) {
		SoundEngine.getSoundEngine().playWinSound();
		
		mainLayout.setBackgroundColor(getResources().getColor(R.color.nicegreen));
		buttonShare.setBackgroundColor(getResources().getColor(R.color.nicegreen));
		buttonBack.setBackgroundColor(getResources().getColor(R.color.nicegreen));
		
		textNewRecordTitle.setVisibility(TextView.VISIBLE);
		textNewRecordTitle.setText(newRecord);
		textFailTitle.setVisibility(TextView.GONE);
		textRecord.setVisibility(TextView.GONE);
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
	
	private void showClassic() {
		// 事实上，只有此模式下，点击会出现失败的现象
		textGameMode.setText("经典模式");

		if (gameState == GameState.GAME_WIN) {
			if (timeRecord < gameRecord.classicRecord) {
				gameRecord.classicRecord = timeRecord;
				showNewRecord(timeRecord + "\"");
			} else {
				showNormalWin(timeRecord + "\"", gameRecord.classicRecord + "\"");
			}
		} else if (gameState == GameState.GAME_OVER){
			showLose();
		}
	}
	
	private void showFaster() {
		textGameMode.setText("街机模式");
		
		if (blockRecord > gameRecord.fasterRecord) {
			gameRecord.fasterRecord = blockRecord;
			showNewRecord(blockRecord + "");
		} else {
			showNormalWin(blockRecord + "", gameRecord.fasterRecord + "");
		}
	}
	
	private void showZen() {
		textGameMode.setText("禅模式");

		if (blockRecord > gameRecord.zenRecord) {
			gameRecord.zenRecord = blockRecord;
			showNewRecord(blockRecord + "");
		} else {
			showNormalWin(blockRecord + "", gameRecord.zenRecord + "");
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		GameRecordKeeper.writeGameRecord(this, gameRecord);
	}
	
	// 用于记录整个游戏的排行数据，通过SharedPerference中保存
	GameRecord gameRecord;
	
	// 传出的游戏数据名称
	private static final String DATA_GAME_MODE = "GameMode";
	private static final String DATA_IS_GAME_RESTART = "GameRestart";
	
	// 传来的游戏数据
	private GameMode gameMode;
	private GameState gameState;
	private float timeRecord;
	private int blockRecord;
	
	// UI Handler
	private LinearLayout mainLayout;
	private Button buttonRestart;
	private Button buttonShare;
	private Button buttonBack;
	private TextView textGameMode;
	private TextView textNewRecordTitle;
	private TextView textFailTitle;
	private TextView textRecord;
	private TextView textBestRecord;
}
