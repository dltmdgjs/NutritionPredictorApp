package com.seunghun.nutritionpredictorapp.data.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.seunghun.nutritionpredictorapp.data.model.DailyLogItem;
import com.seunghun.nutritionpredictorapp.data.model.NutritionTotals;
import com.seunghun.nutritionpredictorapp.data.model.UserProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Locale;

public class IntakeRepository {

    private final AppDbHelper helper;

    public IntakeRepository(Context context) {
        helper = new AppDbHelper(context.getApplicationContext());
    }

    // -------------------------
    // 사용자 프로필 1건 읽기
    // -------------------------
    public UserProfile getUserProfileOrNull() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT age, height_cm, weight_kg, sex, activity FROM user_profile",
                null
        );

        try {
            if (c.moveToFirst()) {
                UserProfile p = new UserProfile();
                p.age = c.getInt(0);
                p.heightCm = c.getFloat(1);
                p.weightKg = c.getFloat(2);
                p.sex = c.getString(3);
                p.activity = c.getString(4);
                return p;
            }
            return null;
        } finally {
            c.close();
        }
    }

    // -------------------------
    // (1) 달력: 특정 월의 "날짜별 음식명 리스트" 조회
    // yearMonth: "yyyy-MM" 형태
    // 반환: date -> labels(list)
    // -------------------------
    public Map<String, List<String>> getMonthFoodLabels(String yearMonth) {
        // 예: yearMonth="2025-12" => date LIKE '2025-12-%'
        SQLiteDatabase db = helper.getReadableDatabase();

        // 1) 날짜별 음식명들을 전부 읽어서 map으로 묶는다.
        Cursor c = db.rawQuery(
                "SELECT date, label FROM intake_log WHERE date LIKE ? ORDER BY date ASC",
                new String[]{ yearMonth + "-%" }
        );

        Map<String, List<String>> map = new HashMap<>();
        try {
            while (c.moveToNext()) {
                String date = c.getString(0);
                String label = c.getString(1);

                if (!map.containsKey(date)) map.put(date, new ArrayList<>());
                map.get(date).add(label);
            }
            return map;
        } finally {
            c.close();
        }
    }

    // -------------------------
    // (2) 일자 상세: 해당 날짜의 "로그 리스트" 조회
    // -------------------------
    public List<DailyLogItem> getDailyLogs(String date) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT date, label, grams, calories, protein, carbs, fats, fiber, sugars, sodium " +
                        "FROM intake_log WHERE date=?",
                new String[]{ date }
        );

        List<DailyLogItem> out = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                DailyLogItem it = new DailyLogItem();
                it.date = c.getString(0);
                it.label = c.getString(1);
                it.grams = c.getInt(2);
                it.calories = c.getInt(3);
                it.protein = c.getInt(4);
                it.carbs = c.getInt(5);
                it.fats = c.getInt(6);
                it.fiber = c.getInt(7);
                it.sugars = c.getInt(8);
                it.sodium = c.getInt(9);
                out.add(it);
            }
            return out;
        } finally {
            c.close();
        }
    }

    // -------------------------
    // (3) 일자 상세: 해당 날짜의 "영양소 총합(SUM)" 조회
    // -------------------------
    public NutritionTotals getDailyTotals(String date) {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT " +
                        "IFNULL(SUM(calories),0), IFNULL(SUM(protein),0), IFNULL(SUM(carbs),0), IFNULL(SUM(fats),0), " +
                        "IFNULL(SUM(fiber),0), IFNULL(SUM(sugars),0), IFNULL(SUM(sodium),0) " +
                        "FROM intake_log WHERE date LIKE ?",
                new String[]{ date + "%" }
        );

        try {
            NutritionTotals t = new NutritionTotals();
            t.date = date;

            if (c.moveToFirst()) {
                t.calories = c.getInt(0);
                t.protein = c.getInt(1);
                t.carbs = c.getInt(2);
                t.fats = c.getInt(3);
                t.fiber = c.getInt(4);
                t.sugars = c.getInt(5);
                t.sodium = c.getInt(6);
            }
            return t;
        } finally {
            c.close();
        }
    }

    // (4) 일자 상세: 일자에 해당하는 음식명들 반환
    public List<String> getDailyFoodNames(String date) {
        SQLiteDatabase db = helper.getReadableDatabase();

        // date가 "yyyy-MM-dd HH:mm:ss" 형태로 저장되어 있을 때 안전하게 LIKE 사용
        Cursor c = db.rawQuery(
                "SELECT label FROM intake_log WHERE date LIKE ?",
                new String[]{ date + "%" }
        );

        List<String> names = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                names.add(c.getString(0));
            }
            return names;
        } finally {
            c.close();
        }
    }
}
