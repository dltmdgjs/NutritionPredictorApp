package com.seunghun.nutritionpredictorapp.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

// TODO: 음식 영양 정보를 얻을 때 중량에 따른 영양정보를 선형 매핑해야함.

public class NutritionRepository {
    private final Map<String, String> nutritionMap = new HashMap<>();

    public NutritionRepository(Context context) {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("nutrition.csv"))
            );
            String line;
            br.readLine(); // header skip
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                nutritionMap.put(tokens[0], line);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    public String getNutrition(String foodName) {
        return nutritionMap.get(foodName);
    }
}
