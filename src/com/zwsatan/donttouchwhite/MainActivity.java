package com.zwsatan.donttouchwhite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;

import com.zwsatan.donttouchwhite.GameView.GameMode;

public class MainActivity extends Activity {

	public MainActivity() {
		mainActivity = this;
	}
	
	public static MainActivity getMainActivity() {
    	return mainActivity;
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initGameData();
		
        initUI();
    }
    
    private void initGameData() {
    	// 获取屏幕大小
    	DisplayMetrics dm = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(dm);
    	SCREEN_WIDTH = dm.widthPixels;
    	SCREEN_HEIGHT = dm.heightPixels;

    	// 由于音效需要加载一堆声音，因此这里先初始化一次音效
    	SoundEngine.getSoundEngine();
    			
    	// 判断是否是重新开始的游戏
    	Intent intent = getIntent();
		boolean isRestartGame = intent.getBooleanExtra(DATA_IS_GAME_RESTART, false);
		if (isRestartGame) {
			// 按钮归位，由于是FrameLayout，进行游戏的时候，按钮设置坐标到屏幕之外了
			backToGame();
			
			GameMode gameMode = (GameMode) intent.getSerializableExtra(DATA_GAME_MODE);
			gameView.startGame(gameMode);
		}
    }
    
    private void initUI() {
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
    			if (SoundEngine.getSoundEngine().isOn()) {
    				SoundEngine.getSoundEngine().setOn(false);
    				buttonMusic.setText("静音");
    			} else {
    				SoundEngine.getSoundEngine().setOn(true);
    				buttonMusic.setText("琴声");
    			}
    		}
    	});
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
	
	// 这两个数据仅仅从WinOrLoseActivity中传过来
	private static final String DATA_GAME_MODE = "GameMode";
	private static final String DATA_IS_GAME_RESTART = "GameRestart";
	
	// 获取屏幕大小，直接变成共有方便使用，暂时就简单放在在此文件中
	public static int SCREEN_WIDTH;
	public static int SCREEN_HEIGHT;
	
	private static MainActivity mainActivity;
	
	private Button buttonClassic;
	private Button buttonFaster;
	private Button buttonZen;
	private Button buttonMusic;
	private GameView gameView;
}

