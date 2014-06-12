package com.zwsatan.donttouchwhite;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGameView();
	}

	public GameView(Context context) {
		super(context);
		initGameView();
	}

	public GameView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGameView();
	}

	private void initGameView() {
		holder = getHolder();
        holder.addCallback(this);
        
        fasterThread = new FasterThread(holder);
        classicOrzenThread = new ClassicOrZenThread(holder);
	}
	
	public void startGame(GameMode mode) {
		gameMode = mode;
		
		if (gameMode == GameMode.GAME_NONE) {
			return;
		}
		
		gameState = GameState.GAME_START;
		
		if (gameMode == GameMode.GAME_CLASSIC) {
        	startClassicGame();
        } else if (gameMode == GameMode.GAME_FASTER) {
        	startFasterGame();
        } else if (gameMode == GameMode.GAME_Zen) {
        	startZenGame();
        }
		
		addStartLine();
		addNormalLine();
		addNormalLine();
		addNormalLine();
		
		initStartBlock();
	}
	
	private void startFasterGame() {
		fasterThread.isRun = true;
		fasterThread.start();	
		
		blockRecord = 0;
		
		speed = 25f;
		speedAcc = 0.02f;
		
		initFasterTouchListner();
	}
	
	private void startZenGame() {
		classicOrzenThread.isRun = true;
		classicOrzenThread.start();
		
		timeRecord = 0;
		timeStart = 0;
		timeTotal = 30;
		blockRecord = 0;
		
		speed = MainActivity.SCREEN_HEIGHT / 4;
		speedAcc = 0f;
		
		initClassicOrZenTouchListener();
	}
	
	private void startClassicGame() {
		classicOrzenThread.isRun = true;
		classicOrzenThread.start();
		
		lineCounts = 0;
		endLineCounts = 0;
		
		timeRecord = 0;
		timeStart = System.currentTimeMillis();
		timeTotal = 0;
		
		speed = MainActivity.SCREEN_HEIGHT / 4;
		speedAcc = 0f;
		
		initClassicOrZenTouchListener();
	}
	
	/**
	 * 设置起始方块，基本作用就是有开始两字
	 */
	private void initStartBlock() {
		for (Block block : blocks) {
			if (block.getColor() == Color.BLACK) {
				block.setStartBlock(true);
				break;
			}
		}
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		if (gameMode == GameMode.GAME_FASTER) {
			fasterThread.isRun = false;
		} else if (gameMode == GameMode.GAME_Zen || gameMode == GameMode.GAME_CLASSIC) {
			classicOrzenThread.isRun = false;
		}
		
		gameMode = GameMode.GAME_NONE;
		gameState = GameState.GAME_UNSTART;
		blocks.clear();
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		if (gameMode == GameMode.GAME_FASTER) {
			fasterThread.isRun = true;
			if (!fasterThread.isAlive()) {
				fasterThread.start();	
			}
		} else if (gameMode == GameMode.GAME_Zen || gameMode == GameMode.GAME_CLASSIC) {
			classicOrzenThread.isRun = true;
			if (!classicOrzenThread.isAlive()) {
				classicOrzenThread.start();
			}
		}
	}
	
	private void moveDown() {
		// 1、所有方块下移
		for (Block block : blocks) {
			block.moveDown(speedAcc);
		}
		
		// 2、根据游戏状态处理不同规则
		if (handleExtraRule()) {
			return;
		}
		
		// 3、移除越界的行
		removeBlockLine();
		
		// 4、添加新的行
		addNewLine();
	}
	
	/**
	 * 移除出界的行
	 */
	private void removeBlockLine() {
		while (true) {
			Block block = blocks.get(0);
			if (block.getY() >= MainActivity.SCREEN_HEIGHT) {
				blocks.remove(0);
			} else {
				break;
			}
		}
	}
	
	/**
	 * 添加新的行
	 */
	private void addNewLine() {
		if (gameMode == GameMode.GAME_CLASSIC) {
			addClassicNewLine();
		} else if (gameMode == GameMode.GAME_FASTER) {
			addFasterNewLine();
		} else if (gameMode == GameMode.GAME_Zen) {
			addZenNewLine();
		}
	}
	
	private void addClassicNewLine() {
		if (lineCounts < WIN_LINES) {
			addNormalLine();
			++lineCounts;
		} else {
			addEndLine();
			++endLineCounts;
		}
		
		if (3 == endLineCounts) {
			gameState = GameState.GAME_WIN;
			showWinOrLose();
		}
	}
	
	private void addFasterNewLine() {
		Block block = blocks.get(blocks.size() - 1);
		if (block.getY() > 0) {
			addNormalLine();
		}
	}
	
	private void addZenNewLine() {
		addNormalLine();
	}
	
	/**
	 * 根据不同的游戏模式，做规则处理
	 * 这里其实就只有 街机模式 需要处理
	 * 当有漏点的方块的时候，游戏会结束
	 */
	private boolean handleExtraRule() {
		if (gameMode == GameMode.GAME_FASTER) {
			
			// 方块其实只用检测越界的那一行，也就是前4个
			for (final Block block : blocks) {
				if (block.getY() < MainActivity.SCREEN_HEIGHT) {
					break;
				}
				
				// 表示有遗漏的方块没有被消除，其他被点击的方块会变成灰色
				if (block.getColor() == Color.BLACK) {
					
					// 所有方块先回退一格
					for (Block tmpBlock : blocks) {
						tmpBlock.setY(tmpBlock.getY() - MainActivity.SCREEN_HEIGHT / 4);
					}
					
					// 设置游戏结束状态
					setGameOver();
					
					// 设置一个计时器，用来延缓一帧来播放黑块没有被点中的动画
					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						
						@Override
						public void run() {
							block.startBlackNotTouchedAnim();
						}
					}, THREAD_SLEEP_TIME + 5);
					
					// 显示游戏结果界面，这里面也是有延缓执行，所以不着急
					showWinOrLose();
					return true;
				}
					
			}
		}
		
		return false;
	}
	
	private void setGameOver() {
		gameState = GameState.GAME_OVER;
		SoundEngine.getSoundEngine().playErrorSound();
	}
	
	/**
	 * 用来检测其实方块是否被点击
	 * @return
	 */
	private boolean isStartBlockTouched(float touchX, float touchY) {
		if (isGameRunning()) {
			return true;
		}
		
		// 如果游戏没运行，表示一定没有点中方块，这里就来检测这种情况
		for (int i = 1; i < 5; ++i) {
			Block block = blocks.get(i);
			if (block.contains(touchX, touchY) && (block.getColor() == Color.BLACK)) {

				gameState = GameState.GAME_RUNNING;
				
				block.startBlackAnim();
				blockRecord++;
				
				return true;
			}
		}
		
		return false;
	}
	
	private void initFasterTouchListner() {
		setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (gameState == GameState.GAME_OVER) {
					return true;
				}
				
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					
					float touchX = event.getX();
					float touchY = event.getY();
					
					// 控制只能点击开始方块
					if (isStartBlockTouched(touchX, touchY)) {
						for (Block block : blocks) {
							if (block.contains(touchX, touchY)) {
								if (block.getColor() == Color.BLACK) {
									block.startBlackAnim();
									blockRecord++;
									
									if (blockRecord % 5 == 0) {
										speedAcc += 0.0005f;
									}
								} else if (block.getColor() == Color.WHITE) {
									block.startWhiteAnim();
									setGameOver();
									showWinOrLose();
									return true;
								}

								break;
							}
						}
					}

				}
				return true;
			}
		});
	}
	
	private void initClassicOrZenTouchListener() {
		setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (gameState == GameState.GAME_OVER || gameState == GameState.GAME_WIN) {
					return true;
				}
					
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					float touchX = event.getX();
					float touchY = event.getY();

					// 控制只能触屏第二行的方块
					int startIndex = 4;
					if (blocks.get(0).getColor() == Color.YELLOW) {
						startIndex = 1;
					}
					
					for (int i = startIndex; i < startIndex + 4; ++i) {
						Block block = blocks.get(i);
						if (block.contains(touchX, touchY)) {
							
							if (block.getColor() == Color.BLACK) {
								block.startBlackAnim();
								blockRecord++;
								
								// 当点击的第一个黑块时游戏开始
								if (gameState != GameState.GAME_RUNNING) {
									gameState = GameState.GAME_RUNNING;
									timeStart = System.currentTimeMillis() + (long) timeTotal * 1000;
								}
								
							} else if (isGameRunning() && block.getColor() == Color.WHITE) {
								block.startWhiteAnim();
								setGameOver();
								showWinOrLose();
								return true;
							}
							
							if (isGameRunning()) {
								moveDown();
							}
							break;
						}
					}
					
				}
				return true;
			}
		});
	}
	
	private void showWinOrLose() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				
				MainActivity activity = MainActivity.getMainActivity();
				Intent intent = new Intent(activity, WinOrLoseActivity.class);
				intent.putExtra("GameMode", gameMode);
				intent.putExtra("GameState", gameState);
				
				if (gameMode == GameMode.GAME_CLASSIC) {
					intent.putExtra("TimeRecord", timeRecord);
				} else if (gameMode == GameMode.GAME_FASTER) {
					intent.putExtra("BlockRecord", blockRecord);
				} else if (gameMode == GameMode.GAME_Zen) {
					intent.putExtra("BlockRecord", blockRecord);
				}
				
				activity.startActivity(intent);
			}
		}, 310);
	}
	
	private void addEndLine() {
		float x = 0;
		float y = 0;
		float width = MainActivity.SCREEN_WIDTH;
		float height = MainActivity.SCREEN_HEIGHT / 4;
		int color = Color.GREEN;
		
		Block block = new Block(x, y, width, height, color, speed);
		blocks.add(block);
	}
	
	private void addStartLine() {
		float x = 0;
		float y = MainActivity.SCREEN_HEIGHT / 4 * 3;
		float width = MainActivity.SCREEN_WIDTH;
		float height = MainActivity.SCREEN_HEIGHT / 4;
		int color = Color.YELLOW;
		
		Block block = new Block(x, y, width, height, color, speed);
		blocks.add(block);
	}
	
	private void addNormalLine() {
		float width = MainActivity.SCREEN_WIDTH / 4;
		float height = MainActivity.SCREEN_HEIGHT / 4;
		float y = blocks.get(blocks.size() - 1).getY() - height;

		Random random = new Random();
		int blackIndex = random.nextInt(4);
		
		for (int i = 0; i < 4; ++i) {
			int color = blackIndex == i ? Color.BLACK : Color.WHITE;
			Block block = new Block(i * width, y, width, height, color, speed);
			blocks.add(block);
		}	
	}

	private boolean isGameRunning() {
		return gameState == GameState.GAME_RUNNING;
	}
	
	private boolean isGameStart() {
		return gameState == GameState.GAME_START;
	}
	
	class FasterThread extends Thread {
		private SurfaceHolder holder;
		private Paint paint = new Paint();
	    public boolean isRun ;
		
	    public  FasterThread(SurfaceHolder holder)
	    {
	        this.holder = holder; 
	        isRun = true;
	    }
	    
		@Override
		public void run() {
			while (isRun) {
				Canvas canvas = null;
	            try
	            {
	            	if (gameState != GameState.GAME_OVER && isGameRunning()) {
	            		speed += speedAcc;
	            		moveDown();
	            	}
					
	                synchronized (holder)
	                {
	                	// 锁定画布，一般在锁定后就可以通过其返回的画布对象Canvas，在其上面画图等操作了。
	                	canvas = holder.lockCanvas();
	                    for (Block block : blocks) {
	                    	block.draw(canvas, paint);
	                    }
	                    
	                    // 绘制集中方块数目
	                    float x = MainActivity.SCREEN_WIDTH / 2;
	                    float y = MainActivity.SCREEN_HEIGHT / 12;
	                    paint.setColor(Color.RED);
	                    paint.setTextSize(96f);
	                    canvas.drawText(blockRecord + "", x, y, paint);
	                    
	                    Thread.sleep(THREAD_SLEEP_TIME);
	                }
	            }
	            catch (Exception e) {
	                e.printStackTrace();
	            }
	            finally
	            {
	                if(canvas != null)
	                {
	                	// 结束锁定画图，并提交改变。
	                    holder.unlockCanvasAndPost(canvas);
	                }
	            }
			}
		}
	}
	
	class ClassicOrZenThread extends Thread {
		private SurfaceHolder holder;
		private Paint paint = new Paint();
	    public boolean isRun ;
	    
	    public  ClassicOrZenThread(SurfaceHolder holder)
	    {
	        this.holder = holder; 
	        isRun = true;
	    }
	    
		@Override
		public void run() {
	        while(isRun)
	        {
	            Canvas canvas = null;
	            try
	            {
	                synchronized (holder)
	                {
	                	// 锁定画布，一般在锁定后就可以通过其返回的画布对象Canvas，在其上面画图等操作了。
	                	canvas = holder.lockCanvas();
	                    for (Block block : blocks) {
	                    	block.draw(canvas, paint);
	                    }
	                    
	                    // 绘制成绩
	                    drawRecord(canvas);

	                    Thread.sleep(THREAD_SLEEP_TIME);
	                }
	            }
	            catch (Exception e) {
	                e.printStackTrace();
	            }
	            finally
	            {
	                if(canvas != null)
	                {
	                	// 结束锁定画图，并提交改变。
	                    holder.unlockCanvasAndPost(canvas);
	                }
	            }
	        }
		}
		
		private void drawRecord(Canvas canvas) {
			paint.setColor(Color.RED);
            paint.setTextSize(96f);
            
            if (isGameStart()) {
            	if (gameMode == GameMode.GAME_CLASSIC) {
            		timeRecord = 0;
            	} else if (gameMode == GameMode.GAME_Zen) {
            		timeRecord = timeTotal;
            	}
            } else if (isGameRunning()) {
            	if (gameMode == GameMode.GAME_CLASSIC) {
            		timeRecord = (System.currentTimeMillis() - timeStart) / 1000.0f;
            	} else if (gameMode == GameMode.GAME_Zen) {
            		timeRecord = (timeStart - System.currentTimeMillis()) / 1000.0f;

            		// 当时间结束时，禅境模式胜利结束
            		if (timeRecord <= 0) {
            			gameState = GameState.GAME_WIN;
            			showWinOrLose();
            			return;
            		}
            	}
            }
            
            float x = MainActivity.SCREEN_WIDTH / 2;
            float y = MainActivity.SCREEN_HEIGHT / 10;
            canvas.drawText(String.format("%.3f\"", timeRecord), x, y, paint);
		}
	}
	
	public enum GameMode {
		GAME_NONE,
		GAME_CLASSIC,
		GAME_FASTER,
		GAME_Zen
	};
	
	public enum GameState {
		GAME_UNSTART,
		GAME_START,
		GAME_RUNNING,
		GAME_OVER,
		GAME_WIN
	};
	
	// 共有参数
	public static SurfaceHolder holder;
	private static final int THREAD_SLEEP_TIME = 20;	// 控制所有线材睡眠20ms，即50帧
	
	private GameMode gameMode = GameMode.GAME_NONE;
	private GameState gameState = GameState.GAME_UNSTART;
	
	private float speed;					// 控制各个游戏模式下的方块下落速度
	private float speedAcc;					// 控制各个游戏模式下方块下落的加速度
	
	private List<Block> blocks = new ArrayList<Block>(); // 方块组集合
	
	// 经典模式下的参数
    private static final int WIN_LINES = 50;// 胜利所需要的所有行数
    private int lineCounts;					// 记录现在一共鼓了多少行
    private int endLineCounts;				// 记录结束行出现了多少行
    private long timeStart;					// 记录游戏起始时间
    private float timeRecord;				// 记录游戏进行时间
    
    // 街机模式下的参数
    private int blockRecord;				// 记录街机模式下当前击中黑块数目
    
    // 禅模式下的参数，该模式下会沿用blockRecord，timeStart和timeRecord参数
    private float timeTotal = 30f;			// 游戏总共时间
    
    private ClassicOrZenThread classicOrzenThread;
    private FasterThread fasterThread;		// 移动线程，此线程中也包含了绘制代码，但不能同时执行绘制线程和街机线材，会导致绘制延迟，考虑如果让move线程每一秒都走，说不定可以
    
    
    
}

