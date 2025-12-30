package com.seunghun.nutritionpredictorapp.ui.day;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.seunghun.nutritionpredictorapp.data.db.IntakeRepository;
import com.seunghun.nutritionpredictorapp.data.model.NutritionTotals;
import com.seunghun.nutritionpredictorapp.data.model.UserProfile;
import com.seunghun.nutritionpredictorapp.databinding.FragmentDayDetailBinding;
import com.seunghun.nutritionpredictorapp.policy.NutritionTargets;
import com.seunghun.nutritionpredictorapp.policy.NutritionTargetsCalculator;
import com.seunghun.nutritionpredictorapp.ui.chart.PieChartUtil;

import java.util.List;

public class DayDetailFragment extends Fragment {
    private static final String ARG_DATE = "arg_date"; // yyyy-MM-dd

    private FragmentDayDetailBinding binding;
    private IntakeRepository repo;

    public static DayDetailFragment newInstance(String date) {
        DayDetailFragment f = new DayDetailFragment();
        Bundle b = new Bundle();
        b.putString(ARG_DATE, date);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDayDetailBinding.inflate(inflater, container, false);
        repo = new IntakeRepository(requireContext());

        String date = getArguments() != null ? getArguments().getString(ARG_DATE, "") : "";
        bind(date);

        return binding.getRoot();
    }

    private void bind(String date) {
        binding.tvDate.setText(date);

        // 1) DB에서 해당 날짜 총합 읽기
        NutritionTotals totals = repo.getDailyTotals(date);

        // 2) 사용자 프로필 읽고 권장 목표치 계산
        UserProfile profile = repo.getUserProfileOrNull();
        if (profile == null) {
            // 프로필이 없으면 차트 표시가 무의미하므로 안내만
            binding.tvAmdrNote.setText("사용자 신체정보가 없습니다. 프로필을 먼저 입력하세요.");
            return;
        }

        // 프로필 기반 권장 섭취량 계산
        NutritionTargets targets = NutritionTargetsCalculator.makeTargets(profile);
        binding.tvAmdrNote.setText(targets.amdrNote);

        // 3) 파이차트 바인딩: "섭취 vs 목표"
        PieChartUtil.bindConsumedVsTarget(binding.pieCalories, "칼로리", totals.calories, targets.targetCaloriesKcal, "kcal");
        PieChartUtil.bindConsumedVsTarget(binding.pieProtein,  "단백질", totals.protein,  targets.targetProteinG,     "g");
        PieChartUtil.bindConsumedVsTarget(binding.pieCarbs,    "탄수화물", totals.carbs,  targets.targetCarbsG,       "g");
        PieChartUtil.bindConsumedVsTarget(binding.pieFats,     "지방",   totals.fats,     targets.targetFatsG,        "g");
        PieChartUtil.bindConsumedVsTarget(binding.pieFiber,    "식이섬유", totals.fiber,  targets.targetFiberG,       "g");
        PieChartUtil.bindConsumedVsTarget(binding.pieSugars,   "당류",   totals.sugars,   targets.targetSugarsG,      "g");
        PieChartUtil.bindConsumedVsTarget(binding.pieSodium,   "나트륨", totals.sodium,   targets.targetSodiumMg,     "mg");

        // 4) 텍스트 요약
        binding.tvSummary.setText(
                "총합: " +
                        totals.calories + "kcal, P " + totals.protein + "g, C " + totals.carbs + "g, F " + totals.fats + "g\n" +
                        "Fiber " + totals.fiber + "g, Sugars " + totals.sugars + "g, Sodium " + totals.sodium + "mg"
        );

        // 5) 음식명
        List<String> foods = repo.getDailyFoodNames(date);
        if (foods == null || foods.isEmpty()) {
            binding.tvFoods.setText("섭취 내역이 없습니다.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i=0; i< foods.size(); i++) {
                sb.append(i+1).append(". ").append(foods.get(i)).append("\n");
            }
            binding.tvFoods.setText(sb.toString());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}