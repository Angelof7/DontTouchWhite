package com.zwsatan.donttouchwhite;

import java.util.Timer;
import java.util.TimerTask;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

public class Block {
	// 方块的位置信息
	private float x;
	private float y;
	private float width;
	private float height;

	// 方块的颜色信息
	private int color;

	// 方块的速度信息
	private float speed;
	
	// 记录黑块被点击动画的缩放的效果
	private float scaleForBlackTouched;
	private int animForWhiteTouched;
	private int animForBlackNotTouched = 0;
	
	// 记录是否为起始方块，就是有两字开始
	private boolean isStartBlock;

	public Block(float x, float y, float width, float height, int color, float speed) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = color;
		this.speed = speed;
		this.scaleForBlackTouched = 0f;
		this.animForWhiteTouched = 0;
		this.isStartBlock = false;
	}

	
	public void setStartBlock(boolean isStartBlock) {
		this.isStartBlock = isStartBlock;
	}

	public float getY() {
		return y;
	}
	
	public void setY(float y) {
		this.y = y;
	}
	
	public int getColor() {
		return color;
	}
	
	public boolean contains(float touchX, float touchY) {
		boolean inX = x <= touchX && touchX <= x + width;
		boolean inY = y <= touchY && touchY <= y + height;
		return inX && inY;
	}
	
	public void moveDown(float speedAcc) {
		y += speed;
		speed += speedAcc;
	}
	
	public void draw(Canvas canvas, Paint paint) {
		
		paint.setColor(color);
		paint.setStyle(Style.FILL);
		canvas.drawRect(x,  y, x + width, y + height, paint);

		// 绘制边框
		if (color != Color.GREEN) {
			paint.setColor(Color.BLACK);
			paint.setStyle(Style.STROKE);
			canvas.drawRect(x,  y, x + width, y + height, paint);
		}
		
		drawBlackTouchAnim(scaleForBlackTouched, canvas, paint);
		
		if (isStartBlock) {
			paint.setColor(Color.WHITE);
			paint.setTextSize(48);
			paint.setTextAlign(Align.CENTER);
			canvas.drawText("开始", x + width / 2, y + height / 2, paint);
		}
	}
	
	
	public void startBlackAnim() {
		ValueAnimator animator = ValueAnimator.ofFloat(0.7f, 1.0f);
		animator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				// 获取当前灰色方块的缩放值
				scaleForBlackTouched = (Float) animator.getAnimatedValue();
				if (scaleForBlackTouched == 1.0f) {
					color = Color.GRAY;
					scaleForBlackTouched = 0f;
				}
			}
		});
		animator.setDuration(200).start();
		
		SoundEngine.getSoundEngine().playPianoSound();
	}
	
	
	public void startBlackNotTouchedAnim() {
		animForBlackNotTouched = 0;
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				if (color == Color.BLACK) {
					color = Color.GRAY;
				} else if (color == Color.GRAY) {
					color = Color.BLACK;
				}
				
				if (6 == animForBlackNotTouched++) {
					timer.cancel();
				}
			}
		}, 0, 50);
	}
	
	public void startWhiteAnim() {
		animForWhiteTouched = 0;
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				if (color == Color.RED) {
					color = Color.WHITE;
				} else if (color == Color.WHITE) {
					color = Color.RED;
				}
				
				if (6 == animForWhiteTouched++) {
					timer.cancel();
				}
			}
		}, 0, 50);
		
//		ValueAnimator animator = ValueAnimator.ofInt(Color.RED, Color.WHITE, Color.RED, Color.WHITE, Color.RED);
//		animator.addUpdateListener(new AnimatorUpdateListener() {
//			
//			@Override
//			public void onAnimationUpdate(ValueAnimator animator) {
//				color = (Integer) animator.getAnimatedValue();
//			}
//		});
//		animator.setDuration(200).start();
	}
	
	public void drawBlackTouchAnim(float scale, Canvas canvas, Paint paint) {
		float newWidth = width * scale;
		float newHeight = height * scale;
		float newX = x + (width - newWidth) / 2;
		float newY = y + (height - newHeight) / 2;
		
		paint = new Paint();
		paint.setColor(Color.GRAY);
		paint.setStyle(Style.FILL);
		canvas.drawRect(newX, newY, newX + newWidth, newY + newHeight, paint);
	}
	
//	public boolean onTouch(float touchX, float touchY) {
//		if (contains(touchX, touchY)) {
//			if (color == Color.BLACK) {
//				startBlackAnim();
//			} else if (color == Color.WHITE) {
//				startWhiteAnim();
//			}
//			
//			return true;
//		}
//		
//		return false;
//	}
	
}
