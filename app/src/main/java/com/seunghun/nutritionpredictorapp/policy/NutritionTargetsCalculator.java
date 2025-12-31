package com.seunghun.nutritionpredictorapp.policy;


import com.seunghun.nutritionpredictorapp.data.model.UserProfile;


// 권장 섭취량 계산기
public class NutritionTargetsCalculator {

    private static final float CARB_TARGET = 0.60f;
    private static final float PROT_TARGET = 0.15f;
    private static final float FAT_TARGET  = 0.25f;

    private static final int DEFAULT_SODIUM_MG = 2000;
    private static final int DEFAULT_FIBER_G_M = 25;
    private static final int DEFAULT_FIBER_G_F = 20;

    // 활동량(PA) 계수
    private static float getPA(String sex, String activity) {
        String s = sex == null ? "남" : sex;
        String a = activity == null ? "앉음" : activity;

        if (s.equals("남")) {
            switch (a) {
                case "저":  return 1.11f;
                case "중":  return 1.25f;
                case "고":  return 1.48f;
                default:   return 1.00f; // 앉음
            }
        } else {
            switch (a) {
                case "저":  return 1.12f;
                case "중":  return 1.27f;
                case "고":  return 1.45f;
                default:   return 1.00f;
            }
        }
    }

    // 에너지 필요추정량(EER) 계산:
    // 남: 662 - 9.53*age + PA*(15.91*weight + 539.6*height(m))
    // 여: 354 - 6.91*age + PA*(9.36*weight + 726*height(m))
    public static int calcEERkcal(UserProfile p) {
        float hM = p.heightCm / 100f;
        float PA = getPA(p.sex, p.activity);

        if ("남".equalsIgnoreCase(p.sex)) {
            double eer = 662 - 9.53 * p.age + PA * (15.91 * p.weightKg + 539.6 * hM);
            return (int) Math.round(eer);
        } else {
            double eer = 354 - 6.91 * p.age + PA * (9.36 * p.weightKg + 726 * hM);
            return (int) Math.round(eer);
        }
    }

    // kcal -> grams 변환: 탄/단 4kcal/g, 지 9kcal/g
    private static int kcalToGrams(float kcal, int kcalPerGram) {
        return (int) Math.round(kcal / kcalPerGram);
    }

    public static NutritionTargets makeTargets(UserProfile p) {
        NutritionTargets t = new NutritionTargets();

        int eer = calcEERkcal(p);
        t.targetCaloriesKcal = eer;

        // 탄단지
        t.targetCarbsG   = kcalToGrams(eer * CARB_TARGET, 4);
        t.targetProteinG = kcalToGrams(eer * PROT_TARGET, 4);
        t.targetFatsG    = kcalToGrams(eer * FAT_TARGET,  9);

        // 식이섬유/나트륨
        t.targetSodiumMg = DEFAULT_SODIUM_MG;
        t.targetFiberG   = "남".equalsIgnoreCase(p.sex) ? DEFAULT_FIBER_G_M : DEFAULT_FIBER_G_F;

        // 당류
        t.targetSugarsG = kcalToGrams(eer * 0.10f, 4);

        t.amdrNote = "AMDR: 탄수 55~65%, 단백질 7~20%, 지방 15~30%";
        return t;
    }
}
