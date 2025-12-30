package com.seunghun.nutritionpredictorapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


// DBHelper 클래스
public class UserInfoDBHelper extends SQLiteOpenHelper {

    public UserInfoDBHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

    public UserInfoDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table myinfo ( ";
        sql += "name text, ";
        sql += "age text, ";
        sql += "height text, ";
        sql += "weight text, ";
        sql += "gender text, ";
        sql += "activity text";
        sql += " )";

        db.execSQL(sql);
    }

    // 테이블 수정시 사용
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists myinfo"); // 기존 테이블 삭제
        onCreate(db); // 새로운 테이블 생성
    }
}
