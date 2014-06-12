package com.zwsatan.donttouchwhite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.zwsatan.donttouchwhite.GameView.GameMode;

public class MainActivity extends Activity {

	public static int SCREEN_WIDTH;
	public static int SCREEN_HEIGHT;
	
	private static MainActivity mainActivity;
	
	private Button buttonClassic;
	private Button buttonFaster;
	private Button buttonZen;
	private Button buttonMusic;
	private GameView gameView;
	
	public MainActivity() {
		mainActivity = this;
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置全屏    
        requestWindowFeature(Window.FEATURE_NO_TITLE);    
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_main);

        // 获取屏幕大小
        DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		SCREEN_WIDTH = dm.widthPixels;
		SCREEN_HEIGHT = dm.heightPixels;
		
		// 初始化按钮
		buttonClassic = (Button) findViewById(R.id.classic_black_button);
		buttonFaster = (Button) findViewById(R.id.faster_white_button);
		buttonZen = (Button) findViewById(R.id.zen_white_button);
		buttonMusic = (Button) findViewById(R.id.music_black_button);
		
		gameView = (GameView) findViewById(R.id.gameview);
		
		buttonClassic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				showButtonOutAnim();
				gameView.startGame(GameMode.GAME_CLASSIC);
			}
		});

		buttonFaster.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				showButtonOutAnim();
				gameView.startGame(GameMode.GAME_FASTER);
			}
		});

		buttonZen.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				showButtonOutAnim();
				gameView.startGame(GameMode.GAME_Zen);
			}
		});

		buttonMusic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// 音乐暂时不处理
			}
		});
		
		// 这里先初始化一次音效
		SoundEngine.getSoundEngine();
		
		Intent intent = getIntent();
		boolean isRestartGame = intent.getBooleanExtra("RestartGame", false);
		if (isRestartGame) {
			GameMode gameMode = (GameMode) intent.getSerializableExtra("GameMode");
			backToGame();
			gameView.startGame(gameMode);
		}
    }
    
    public static MainActivity getMainActivity() {
    	return mainActivity;
    }
    
    /**
	 * 点击按钮以后，四个按钮分左右淡出屏幕
	 * 同时对应的游戏界面显示出来
	 */
	private void showButtonOutAnim() {
		// 定义淡出的坐标，实际只有x坐标的改变
		// 同时由于只有四个方块，因此此处可以直接设定为固定值
		final int leftOffset = -SCREEN_WIDTH / 2;
		final int rightOffset = -leftOffset;
		final int duration = 500;
		
		TranslateAnimation leftAnim = new TranslateAnimation(0, leftOffset, 0, 0);
		leftAnim.setDuration(duration);
		
		TranslateAnimation rightAnim = new TranslateAnimation(0, rightOffset, 0, 0);
		rightAnim.setDuration(duration);
		
		leftAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {
				backToGame();
			}
		});
		
		buttonClassic.startAnimation(leftAnim);
		buttonFaster.startAnimation(rightAnim);
		buttonZen.startAnimation(leftAnim);
		buttonMusic.startAnimation(rightAnim);
	}
	
	/**
	 * 调整由于动画导致淡出的按钮。遮挡游戏界面
	 */
	public void backToStartMenu() {
		buttonClassic.setX(0);
		buttonFaster.setX(SCREEN_WIDTH / 2);
		buttonZen.setX(0);
		buttonMusic.setX(SCREEN_WIDTH / 2);
		
		buttonClassic.setEnabled(true);
		buttonFaster.setEnabled(true);
		buttonZen.setEnabled(true);
		buttonMusic.setEnabled(true);
	}
	
	/**
	 * 调整按钮坐标到屏幕之外，让游戏界面出现
	 */
	public void backToGame() {
		buttonClassic.setX(-SCREEN_WIDTH / 2);
		buttonFaster.setX(SCREEN_WIDTH);
		buttonZen.setX(-SCREEN_WIDTH / 2);
		buttonMusic.setX(SCREEN_WIDTH);
		
		buttonClassic.setEnabled(false);
		buttonFaster.setEnabled(false);
		buttonZen.setEnabled(false);
		buttonMusic.setEnabled(false);
	}
}

