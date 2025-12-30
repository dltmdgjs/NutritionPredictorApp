package com.seunghun.nutritionpredictorapp.ui.chart;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieData;

import java.util.ArrayList;
import java.util.List;

public class PieChartUtil {
    public static void bindConsumedVsTarget(
            PieChart chart,
            String title,
            float consumed,
            float target,
            String unit
    ) {
        chart.getDescription().setEnabled(false);
        chart.setUsePercentValues(false);
        chart.setCenterText(title + "\n" + (int)consumed + "/" + (int)target + unit);
        chart.setDrawEntryLabels(false);

        List<PieEntry> entries = new ArrayList<>();

        if (target <= 0) {
            entries.add(new PieEntry(1f, "N/A"));
        } else if (consumed <= target) {
            entries.add(new PieEntry(consumed, "섭취"));
            entries.add(new PieEntry(target - consumed, "남음"));
        } else {
            // 초과: "목표치" + "초과분"으로 보여주면 사용자가 직관적으로 이해합니다.
            entries.add(new PieEntry(target, "목표"));
            entries.add(new PieEntry(consumed - target, "초과"));
        }

        PieDataSet ds = new PieDataSet(entries, "");
        // 색상은 프로젝트 테마에 맞춰 정하세요. (여기선 기본 팔레트 사용)
        ds.setColors(new int[]{
                0xFF4CAF50, // green
                0xFFBDBDBD, // gray
                0xFFF44336  // red
        });

        PieData data = new PieData(ds);
        data.setDrawValues(false); // 값 라벨은 가운데 텍스트로만 표시(깔끔)
        chart.setData(data);
        chart.invalidate();
    }
}
