package com.zwsatan.donttouchwhite;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class GameRecordKeeper {
	
	public static void writeGameRecord(Context context, GameRecord gameRecord) {
		if (null == context) {
			return;
		}
		
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
		Editor editor = pref.edit();
		editor.putFloat(CLASSIC_TIME_RECORD, gameRecord.classicRecord);
		editor.putInt(FASTER_BLOCK_RECORD, gameRecord.fasterRecord);
		editor.putInt(ZEN_BLOCK_RECORD, gameRecord.zenRecord);
		editor.commit();
	}
	
	public static GameRecord readGameRecord(Context context) {
		if (null == context) {
			return null;
		}
		
		GameRecord gameRecord = new GameRecord();
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
		gameRecord.classicRecord = pref.getFloat(CLASSIC_TIME_RECORD, 9999f);
		gameRecord.fasterRecord = pref.getInt(FASTER_BLOCK_RECORD, 0);
		gameRecord.zenRecord = pref.getInt(ZEN_BLOCK_RECORD, 0);
		
		return gameRecord;
	}
	
	private static final String PREFERENCES_NAME = "com_zwsatan_donttouchwhite";

	private static final String CLASSIC_TIME_RECORD = "TimeRecord";
	private static final String FASTER_BLOCK_RECORD = "FasterBlockRecord";
	private static final String ZEN_BLOCK_RECORD = "ZenBlockRecord";
}

