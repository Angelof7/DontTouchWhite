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
	
	public static SurfaceHolder holder;
	
	private GameMode gameMode = GameMode.GAME_NONE;
	private GameState gameState = GameState.GAME_UNSTART;

	// 共有参数
	private static final int THREAD_SLEEP_TIME = 20;	// 控制所有线材睡眠20ms，即50帧
	private float speed;					// 控制各个游戏模式下的方块下落速度
	private float speedAcc;					// 控制各个游戏模式下方块下落的加速度
	
	// 经典模式下的参数
    private ClassicThread drawThread;		// 绘制线程
    private static final int WIN_LINES = 50;// 胜利所需要的所有行数
    private int lineCounts;					// 记录现在一共鼓了多少行
    private int endLineCounts;				// 记录结束行出现了多少行
    private long timeStart;					// 记录游戏起始时间
    private float timeRecord;				// 记录游戏进行时间
    
    // 街机模式下的参数
    private FasterThread moveThread;		// 移动线程，此线程中也包含了绘制代码，但不能同时执行绘制线程和街机线材，会导致绘制延迟
    										// 考虑如果让move线程每一秒都走，说不定可以
    private int blockRecord;				// 记录街机模式下当前击中黑块数目
    
    // 禅模式下的参数
    private ZenThread zenThread;
    private float timeTotal = 30f;			// 游戏总共时间
    
    // 方块组集合
    private List<Block> blocks = new ArrayList<Block>();
    
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
		holder = this.getHolder();
        holder.addCallback(this);
        drawThread = new ClassicThread(holder);//创建一个绘图线程
        moveThread = new FasterThread(holder);
        zenThread = new ZenThread(holder);
	}
	
	public void startGame(GameMode mode) {
		gameMode = mode;
		
		if (mode == GameMode.GAME_NONE) {
			return;
		}
		
		if (gameMode == GameMode.GAME_CLASSIC) {
        	gameState = GameState.GAME_START;
        	drawThread.isRun = true;
			drawThread.start();
        	startClassicGame();
        } else if (gameMode == GameMode.GAME_FASTER) {
        	gameState = GameState.GAME_START;
        	moveThread.isRun = true;
			moveThread.start();	
        	startFasterGame();
        } else if (gameMode == GameMode.GAME_Zen) {
        	zenThread.isRun = true;
			zenThread.start();
        	gameState = GameState.GAME_START;
        	startZenGame();
        }
		
		if (gameState == GameState.GAME_START) {
			for (Block block : blocks) {
				if (block.getColor() == Color.BLACK) {
					block.setStartBlock(true);
					break;
				}
			}
		}
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		if (gameMode == GameMode.GAME_CLASSIC) {
			drawThread.isRun = false;
		} else if (gameMode == GameMode.GAME_FASTER) {
			moveThread.isRun = false;
		} else if (gameMode == GameMode.GAME_Zen) {
			zenThread.isRun = false;
		}
		
		blocks.clear();
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		if (gameMode == GameMode.GAME_CLASSIC) {
			drawThread.isRun = true;
			if (!drawThread.isAlive()) {
				drawThread.start();
			}
		} else if (gameMode == GameMode.GAME_FASTER) {
			moveThread.isRun = true;
			if (!moveThread.isAlive()) {
				moveThread.start();	
			}
		} else if (gameMode == GameMode.GAME_Zen) {
			zenThread.isRun = true;
			if (!zenThread.isAlive()) {
				zenThread.start();
			}
		}
	}
	
	private void moveDown() {
		// 所有方块下移
		for (Block block : blocks) {
			block.moveDown(speedAcc);
		}
		
		if (gameMode == GameMode.GAME_FASTER) {
			for (int i1 = 0; i1 < blocks.size(); ++i1) {
				final Block block = blocks.get(i1);
				if (block.getY() >= MainActivity.SCREEN_HEIGHT) {

					// 表示有遗漏的方块没有被消除
					if (block.getColor() == Color.BLACK) {

						for (int i = 0; i < blocks.size(); ++i) {
							Block b = blocks.get(i);
							b.setY(b.getY() - MainActivity.SCREEN_HEIGHT / 4);
						}

						setGameOver();
						
						Timer timer = new Timer();
						timer.schedule(new TimerTask() {
							
							@Override
							public void run() {
								block.startBlackNotTouchedAnim();
							}
						}, THREAD_SLEEP_TIME + 5);
						
						showWinOrLose();
						return;
					}

				} else {
					break;
				}
			}
		}
		
		// 移除出界的行
		while (true) {
			Block block = blocks.get(0);
			if (block.getY() >= MainActivity.SCREEN_HEIGHT) {
				blocks.remove(0);
			} else {
				break;
			}
		}
		
		// 添加新的行
		if (gameMode == GameMode.GAME_CLASSIC) {
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
		} else if (gameMode == GameMode.GAME_FASTER) {
			Block block = blocks.get(blocks.size() - 1);
			if (block.getY() > 0) {
				addNormalLine();
			}
		} else if (gameMode == GameMode.GAME_Zen) {
				addNormalLine();
		}
		
		invalidate();
	}
	
	private void setGameOver() {
		gameState = GameState.GAME_OVER;
		SoundEngine.getSoundEngine().playErrorSound();
	}
	
	private void startFasterGame() {
		blockRecord = 0;
		
		speed = 25f;
		speedAcc = 0.02f;
		
		initFasterTouchListner();
		
		addStartLine();
		addNormalLine();
		addNormalLine();
		addNormalLine();
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
					if (!isGameRunning()) {
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
									}

								} else if (isGameRunning() && block.getColor() == Color.WHITE) {
									block.startWhiteAnim();
									setGameOver();
									showWinOrLose();
									return true;
								}

								break;
							}
						}
					} else {
						
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
	
	private void startZenGame() {
		timeRecord = 0;
		timeStart = 0;
		blockRecord = 0;
		
		speed = MainActivity.SCREEN_HEIGHT / 4;
		speedAcc = 0f;
		
		initZenTouchListener();
		
		addStartLine();
		addNormalLine();
		addNormalLine();
		addNormalLine();
	}
	
	private void initZenTouchListener() {
		setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (gameState == GameState.GAME_OVER) {
					return true;
				}
				
				if (gameState == GameState.GAME_WIN) {
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
	
	private void startClassicGame() {
		lineCounts = 0;
		endLineCounts = 0;
		
		timeRecord = 0;
		timeStart = System.currentTimeMillis();
		
		speed = MainActivity.SCREEN_HEIGHT / 4;
		speedAcc = 0f;
		
		initClassicTouchListener();
		
		addStartLine();
		addNormalLine();
		addNormalLine();
		addNormalLine();
	}
	
	private void initClassicTouchListener() {
		setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (gameState == GameState.GAME_OVER) {
					return true;
				}
				
				if (gameState == GameState.GAME_WIN) {
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
									timeStart = System.currentTimeMillis();
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
	
	class ClassicThread extends Thread {
		private SurfaceHolder holder;
		private Paint paint = new Paint();
	    public boolean isRun ;
	    
	    public  ClassicThread(SurfaceHolder holder)
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
	                    paint.setColor(Color.RED);
	                    paint.setTextSize(96f);
	                    
	                    if (gameState == GameState.GAME_RUNNING) {
	                    	timeRecord = (System.currentTimeMillis() - timeStart) / 1000.0f;
	                    } else if (gameState == GameState.GAME_START){
	                    	timeRecord = 0;
	                    }
	                    
	                    float x = MainActivity.SCREEN_WIDTH / 2;
	                    float y = MainActivity.SCREEN_HEIGHT / 10;
	                    canvas.drawText(String.format("%.3f\"", timeRecord), x, y, paint);

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
	
	class ZenThread extends Thread {
		private SurfaceHolder holder;
		private Paint paint = new Paint();
	    public boolean isRun ;
	    
	    public  ZenThread(SurfaceHolder holder)
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
	                    paint.setColor(Color.RED);
	                    paint.setTextSize(96f);
	                    
	                    if (gameState == GameState.GAME_RUNNING) {
	                    	timeRecord = (timeStart - System.currentTimeMillis()) / 1000.0f;
	                    	
	                    	if (timeRecord <= 0) {
	                    		gameState = GameState.GAME_WIN;
	                    		showWinOrLose();
	                    		return;
	                    	}
	                    	
	                    } else if (gameState == GameState.GAME_START){
	                    	timeRecord = timeTotal;
	                    }
	                    
	                    float x = MainActivity.SCREEN_WIDTH / 2;
	                    float y = MainActivity.SCREEN_HEIGHT / 10;
	                    canvas.drawText(String.format("%.3f\"", timeRecord), x, y, paint);

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
}

