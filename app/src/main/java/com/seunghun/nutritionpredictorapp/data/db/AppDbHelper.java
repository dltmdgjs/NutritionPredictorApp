package com.seunghun.nutritionpredictorapp.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// 캘린더 프레그먼트에서 사용할 정보들을 따로 DB에 저장하였음
// TODO: DB 단일화 필요 (연결된 fragment들 DB관련 메서드 수정)
public class AppDbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "nutrition.db";
    public static final int DB_VER = 1;

    public AppDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 이미 구현되어 있다면 생략/수정
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS user_profile (" +
                        "age INTEGER," +
                        "height_cm REAL," +
                        "weight_kg REAL," +
                        "sex TEXT," +
                        "activity TEXT" +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS intake_log (" +
                        "date TEXT," +        // yyyy-MM-dd
                        "label TEXT," +
                        "grams INTEGER," +
                        "calories INTEGER," +
                        "protein INTEGER," +
                        "carbs INTEGER," +
                        "fats INTEGER," +
                        "fiber INTEGER," +
                        "sugars INTEGER," +
                        "sodium INTEGER" +
                        ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // 마이그레이션 정책에 맞게 처리
    }
}
