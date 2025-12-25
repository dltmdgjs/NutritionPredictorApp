package com.seunghun.nutritionpredictorapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// 섭취량 정보 저장 DBHelper 클래스
public class IntakeInfoDBHelper extends SQLiteOpenHelper {
    public IntakeInfoDBHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

    public IntakeInfoDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table intakeinfo (";
        sql += "date text,"; // 날짜
        sql += "food text,"; // 음식명
        sql += "gram integer,";
        sql += "calories integer,";
        sql += "protein integer,";
        sql += "carbohydrates integer,";
        sql += "fats integer,";
        sql += "fiber integer,";
        sql += "sugars integer,";
        sql += "sodium integer";
        sql += ")"; // 영양 정보들

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists intakeinfo");
        onCreate(db);
    }
}
