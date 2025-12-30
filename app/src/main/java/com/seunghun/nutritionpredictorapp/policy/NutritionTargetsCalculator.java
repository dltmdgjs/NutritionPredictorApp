package com.seunghun.nutritionpredictorapp.policy;


import com.seunghun.nutritionpredictorapp.data.model.UserProfile;
import java.util.Locale;


// 권장 섭취량 계산기
public class NutritionTargetsCalculator {
    // AMDR 범위(국내 정리 문헌에 흔히 제시되는 범위)  :contentReference[oaicite:4]{index=4}
    // 탄수화물 55~65%, 단백질 7~20%, 지방 15~30%
    private static final float CARB_MIN = 0.55f, CARB_MAX = 0.65f;
    private static final float PROT_MIN = 0.07f, PROT_MAX = 0.20f;
    private static final float FAT_MIN  = 0.15f, FAT_MAX  = 0.30f;

    // 앱에서 "목표치"로 쓸 기본 비율(중간값 근처 추천)
    private static final float CARB_TARGET = 0.60f;
    private static final float PROT_TARGET = 0.15f;
    private static final float FAT_TARGET  = 0.25f;

    // (선택) 기본 상한/목표치 예시: 나트륨/당/식이섬유 등
    // 실제 정책을 더 엄밀히 반영하려면 KDRI 표를 연령/성별별로 매핑해도 됩니다.
    private static final int DEFAULT_SODIUM_MG = 2000; // 예시(상한/목표치로 자주 쓰는 값)
    private static final int DEFAULT_FIBER_G_M = 25;   // 예시
    private static final int DEFAULT_FIBER_G_F = 20;   // 예시
    // 당류는 "총 에너지의 10% 이하" 같은 형태가 많아 g로 환산해서 사용 가능

    // 활동량(PA) 계수 (실제 프로젝트에서는 UI에서 activity 선택지를 이 값에 연결)
    // KDRI 계열 문헌에서 소개되는 IOM 형태 EER 수식 사용 :contentReference[oaicite:5]{index=5}
    private static float getPA(String sex, String activity) {
        String s = sex == null ? "M" : sex.toUpperCase(Locale.US);
        String a = activity == null ? "SEDENTARY" : activity.toUpperCase(Locale.US);

        // “대략적인 기본값”을 제공하되, 실제 앱 정책에 맞춰 쉽게 조정 가능하도록 분리
        if (s.equals("M")) {
            switch (a) {
                case "LOW_ACTIVE":  return 1.11f;
                case "ACTIVE":      return 1.25f;
                case "VERY_ACTIVE": return 1.48f;
                default:            return 1.00f; // SEDENTARY
            }
        } else {
            switch (a) {
                case "LOW_ACTIVE":  return 1.12f;
                case "ACTIVE":      return 1.27f;
                case "VERY_ACTIVE": return 1.45f;
                default:            return 1.00f;
            }
        }
    }

    // 에너지 필요추정량(EER) 계산:
    // 남: 662 - 9.53*age + PA*(15.91*weight + 539.6*height(m))
    // 여: 354 - 6.91*age + PA*(9.36*weight + 726*height(m))
    // :contentReference[oaicite:6]{index=6}
    public static int calcEERkcal(UserProfile p) {
        float hM = p.heightCm / 100f;
        float PA = getPA(p.sex, p.activity);

        if ("M".equalsIgnoreCase(p.sex)) {
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

        // 목표 비율(중간값)
        t.targetCarbsG   = kcalToGrams(eer * CARB_TARGET, 4);
        t.targetProteinG = kcalToGrams(eer * PROT_TARGET, 4);
        t.targetFatsG    = kcalToGrams(eer * FAT_TARGET,  9);

        // 식이섬유/나트륨/당류 등은 “정책 테이블을 붙이는 방식”이 가장 정확합니다.
        // 일단은 성별 기본치(예시)로 넣고, 추후 KDRI 테이블로 고도화하면 됩니다.
        t.targetSodiumMg = DEFAULT_SODIUM_MG;
        t.targetFiberG   = "M".equalsIgnoreCase(p.sex) ? DEFAULT_FIBER_G_M : DEFAULT_FIBER_G_F;

        // 당류 목표 예시: 에너지의 10%를 당으로 제한 => grams = (eer*0.10)/4
        t.targetSugarsG = kcalToGrams(eer * 0.10f, 4);

        t.amdrNote = "AMDR: 탄수 55~65%, 단백질 7~20%, 지방 15~30%";
        return t;
    }
}
