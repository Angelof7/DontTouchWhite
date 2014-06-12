package com.zwsatan.donttouchwhite;

import java.util.Random;

import android.media.AudioManager;
import android.media.SoundPool;

public class SoundEngine {

	public static SoundEngine getSoundEngine() {
		if (soundEngine == null) {
			soundEngine = new SoundEngine();
		}

		return soundEngine;
	}

	private SoundEngine() {
		// 鍒濆鍖栭煶鏁�
		winSound = soundPool.load(MainActivity.getMainActivity(), R.raw.win, 1);
		errorSound = soundPool.load(MainActivity.getMainActivity(), R.raw.error, 1);

		paino = new int[] {
				soundPool.load(MainActivity.getMainActivity(), R.raw.soundt, 1),
				soundPool.load(MainActivity.getMainActivity(), R.raw.soundu, 1),
				soundPool.load(MainActivity.getMainActivity(), R.raw.soundv, 1),
				soundPool.load(MainActivity.getMainActivity(), R.raw.soundw, 1),
				soundPool.load(MainActivity.getMainActivity(), R.raw.soundx, 1),
				soundPool.load(MainActivity.getMainActivity(), R.raw.soundy, 1),
				soundPool.load(MainActivity.getMainActivity(), R.raw.soundz, 1)
		};

		Random random = new Random();
		currentMusicIndex = random.nextInt(2);
		currentMusicSoundIndex = 0;
	}

	public void setOn(boolean isOn) {
		this.isOn = isOn;
	}

	public boolean isOn() {
		return isOn;
	}

	public void playWinSound() {
		if (isOn) {
			soundPool.play(winSound, 1, 1, 0, 0, 1);
		}
	}

	public void playErrorSound() {
		if (isOn) {
			soundPool.play(errorSound, 1, 1, 0, 0, 1);
		}
	}

	public void playPianoSound() {
		if (isOn) {
			boolean isMusicDone = MUSIC[currentMusicIndex].length == currentMusicSoundIndex;

			if (isMusicDone) {
				Random random = new Random(); 
				currentMusicIndex = random.nextInt(2);
				currentMusicSoundIndex = 0;
			}

			soundPool.play(paino[MUSIC[currentMusicIndex][currentMusicSoundIndex++] - 1], 1, 1, 0, 0, 1);
		}
	}

	private static SoundEngine soundEngine = null;

	private SoundPool soundPool = new SoundPool(2, AudioManager.STREAM_SYSTEM , 8);
	private boolean isOn = true;
	private int winSound;
	private int errorSound;
	private int[] paino;
	private int currentMusicIndex;
	private int currentMusicSoundIndex;

	private final int[] KISS_THE_RAIN = new int[] 
			{
			5, 1, 2, 2, 3, 3, 1, 2, 3, 2, 5, 5, 5, 6, 7, 7, 1, 1, 2, 3, 2, 1, 7, 1, 7, 5, 5, 6, 6, 5, 4, 4, 5, 5, 1, 2, 3, 4, 4,
			5, 4, 3, 2, 5, 1, 2, 2,
			3, 3, 1, 2, 3, 2, 5, 5, 5, 6, 7, 7, 1, 1, 2, 3, 2, 1, 7, 1, 7, 5, 5, 6, 6, 5, 4, 4, 5, 5, 1, 2, 3, 4, 6, 1, 7, 1,
			1, 3, 5, 6, 7, 1, 6, 5, 7, 1, 5, 5, 4, 4, 3, 3, 2, 2, 1, 2, 3, 3,
			1, 3, 5, 6, 7, 7, 6, 5, 3, 4, 5, 4, 3, 4, 5, 6, 7, 1, 3, 2
			};

	private final int[] CITY_OF_SKY = new int[]
			{
			4, 3, 4, 1, 3 , 3, 1, 1, 1, 7, 4, 4, 7, 7, 6, 7,
			1, 7, 1, 3, 7 , 3 , 6, 5, 6, 1 , 5 , 3, 3
			};

	private final int[] TEST = new int[]
			{
			1, 2, 3, 4, 5, 6, 7
			};

	private final int[][] MUSIC = new int[][] {
			KISS_THE_RAIN, CITY_OF_SKY, TEST
	};
}
