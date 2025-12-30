package com.seunghun.nutritionpredictorapp.policy;

public class NutritionTargets {
    public int targetCaloriesKcal;

    public int targetProteinG;
    public int targetCarbsG;
    public int targetFatsG;

    public int targetFiberG;   // 선택: 정책/기준치가 있으면 반영
    public int targetSugarsG;  // 선택: 상한(예: 에너지의 10% 이하 등)
    public int targetSodiumMg; // 선택: 상한/목표치

    // 참고 표시용(범위 텍스트)
    public String amdrNote; // 예: "AMDR: C 55~65%, P 7~20%, F 15~30%"
}
