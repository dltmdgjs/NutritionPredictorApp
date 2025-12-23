package com.seunghun.nutritionpredictorapp.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

// 영양 정보 계산
public class NutritionCalculator {
    // 영양 정보 클래스 (계산된 영양정보를 담음)
    public static class NutritionInfo {
        // weight,calories,protein,carbohydrates,fats,fiber,sugars,sodium
        public final int weight,calories,protein,carbohydrates,fats,fiber,sugars,sodium;

        public NutritionInfo(int weight, int calories, int protein, int carbohydrates, int fats, int fiber, int sugars, int sodium) {
            this.weight = weight;
            this.calories = calories;
            this.protein = protein;
            this.carbohydrates = carbohydrates;
            this.fats = fats;
            this.fiber = fiber;
            this.sugars = sugars;
            this.sodium = sodium;
        }
    }
    // Row 클래스 (nutrition.csv의 한 줄)
    private static class Row {
        String label;
        int weight;
        int calories, protein, carbohydrates, fats, fiber, sugars, sodium;
    }

    // label별 영양 정보 Row들 저장
    private final HashMap<String, List<Row>> db = new HashMap<>();

    // 로더
    public static NutritionCalculator fromAssets(Context context, String assetCsvName) {
        NutritionCalculator calc = new NutritionCalculator();
        calc.loadCsv(context, assetCsvName);
        return calc;
    }

    // assets에 있는 nutrition.csv를 한 줄씩 읽음
    // label 별로 묶은 뒤, 각 label의 데이터가 weight 오름차순으로 정렬됨
    // (나중에 getNutririon()에서 안정적으로 값을 얻기 위함.)
    private void loadCsv(Context context, String assetCsvName) {
        // 파일 열기 (안전하게 자동으로 닫음 - 리소스 누수 방지)
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(context.getAssets().open(assetCsvName))
        )) {
            // 첫 줄(헤더, column명) 버림
            String header = br.readLine();
            // 빈 파일 체크
            if (header == null) throw new RuntimeException("nutrition.csv is empty");

            // 한 줄씩 읽어 Row로 변환
            String line;
            while ((line = br.readLine()) != null) {
                // 콤마로 구분
                String[] t = line.split(",");
                if (t.length < 9) continue; // 한 줄의 컬럼 수가 9개 보다 적은 경우 그 줄은 무시(깨진 데이터)

                // 각 컬럼(라벨, 중량, 영양성분)을 Row 객체에 채움
                Row r = new Row();
                r.label = t[0].trim();
                r.weight = parseIntSafe(t[1]);
                r.calories = parseIntSafe(t[2]);
                r.protein = parseIntSafe(t[3]);
                r.carbohydrates = parseIntSafe(t[4]);
                r.fats = parseIntSafe(t[5]);
                r.fiber = parseIntSafe(t[6]);
                r.sugars = parseIntSafe(t[7]);
                r.sodium = parseIntSafe(t[8]);

                // label 별로 묶어서 db에 저장함.
                db.computeIfAbsent(r.label, k -> new ArrayList<>()).add(r);
            }

            // label 별로 weight 오름차순 정렬
            for (String k : db.keySet()) {
                Collections.sort(db.get(k), Comparator.comparingInt(o -> o.weight));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load nutrition.csv from assets", e);
        }
    }

    // CSV에 저장된 숫자 문자열을 int로 안전하게 변환 (소수점은 반올림, 변환 실패시 0를 반환)
    private int parseIntSafe(String s) {
        try {
            return (int) Math.round(Double.parseDouble(s.trim()));
        } catch (Exception e) {
            return 0;
        }
    }

    // =========================
    // 핵심 API
    // label + inputGrams -> NutritionInfo(int)
    // =========================
    public NutritionInfo getNutrition(String label, int inputGrams) {
        // 입력받은 라벨, 중량이 올바르지 않은 경우 -> null
        if (label == null || label.isEmpty()) return null;
        if (inputGrams <= 0) return null;

        //
        List<Row> rows = db.get(label);
        if (rows == null || rows.isEmpty()) return null;

        // 1) 정확히 같은 중량인 경우 그대로 반환
        for (Row r : rows) {
            if (r.weight == inputGrams) {
                return new NutritionInfo(
                        inputGrams,
                        r.calories, r.protein, r.carbohydrates, r.fats,
                        r.fiber, r.sugars, r.sodium
                );
            }
        }

        // 2) 범위 체크
        Row min = rows.get(0);
        Row max = rows.get(rows.size() - 1);

        // 2-1) 범위 밖인 경우, 가장 가까운 경계 row를 기준으로 비율 스케일링
        if (inputGrams < min.weight) {
            return ratioScale(min, inputGrams);
        }
        if (inputGrams > max.weight) {
            return ratioScale(max, inputGrams);
        }

        // 2-2) 범위 안인 경우, 두 이웃 row를 찾음
        Row low = null, high = null;
        for (int i = 0; i < rows.size() - 1; i++) {
            Row a = rows.get(i);
            Row b = rows.get(i + 1);
            if (a.weight < inputGrams && inputGrams < b.weight) {
                low = a; high = b;
                break;
            }
        }
        // 2-2) 이웃을 못찾은 경우, 가장 가까운 것 비율 스케일 (예외상황)
        if (low == null || high == null) {
            Row nearest = nearestRow(rows, inputGrams);
            return ratioScale(nearest, inputGrams);
        }
        // 2-2) 이웃을 찾은 경우, 선형 보간
        return lerp(low, high, inputGrams);
    }

    // 가장 가까운 것 비율 스케일
    private Row nearestRow(List<Row> rows, int w) {
        Row best = rows.get(0);
        int bestDiff = Math.abs(best.weight - w);
        for (Row r : rows) {
            int d = Math.abs(r.weight - w);
            if (d < bestDiff) { bestDiff = d; best = r; }
        }
        return best;
    }

    // 비율 계산: value * (input / baseWeight)
    private NutritionInfo ratioScale(Row base, int inputGrams) {
        double ratio = (double) inputGrams / (double) base.weight;
        return new NutritionInfo(
                inputGrams,
                (int) Math.round(base.calories * ratio),
                (int) Math.round(base.protein * ratio),
                (int) Math.round(base.carbohydrates * ratio),
                (int) Math.round(base.fats * ratio),
                (int) Math.round(base.fiber * ratio),
                (int) Math.round(base.sugars * ratio),
                (int) Math.round(base.sodium * ratio)
        );
    }

    // 선형 보간: a + t*(b-a), t = (w-aw)/(bw-aw)
    private NutritionInfo lerp(Row a, Row b, int w) {
        double t = (double) (w - a.weight) / (double) (b.weight - a.weight);

        int calories = (int) Math.round(a.calories + t * (b.calories - a.calories));
        int protein  = (int) Math.round(a.protein + t * (b.protein - a.protein));
        int carbs    = (int) Math.round(a.carbohydrates + t * (b.carbohydrates - a.carbohydrates));
        int fats     = (int) Math.round(a.fats + t * (b.fats - a.fats));
        int fiber    = (int) Math.round(a.fiber + t * (b.fiber - a.fiber));
        int sugars   = (int) Math.round(a.sugars + t * (b.sugars - a.sugars));
        int sodium   = (int) Math.round(a.sodium + t * (b.sodium - a.sodium));

        return new NutritionInfo(w, calories, protein, carbs, fats, fiber, sugars, sodium);
    }
}
