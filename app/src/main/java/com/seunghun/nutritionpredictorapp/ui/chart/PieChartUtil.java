package com.seunghun.nutritionpredictorapp.ui.chart;

import android.graphics.Color;
import android.graphics.Typeface;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.formatter.ValueFormatter;

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
        chart.setDrawEntryLabels(false);
        chart.setHoleRadius(60f);      // 가운데 구멍 크기
        chart.setTransparentCircleRadius(65f);
        chart.setEntryLabelTextSize(12f);
        chart.setCenterTextSize(14f);
        chart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (target <= 0) {
            entries.add(new PieEntry(1f, "N/A"));
            colors.add(0xFFBDBDBD); // gray
        } else if (consumed <= target) {
            entries.add(new PieEntry(consumed, "섭취"));
            entries.add(new PieEntry(target - consumed, "남음"));
            colors.add(0xFF4CAF50); // green
            colors.add(0xFFBDBDBD); // gray
        } else {
            entries.add(new PieEntry(target, "목표"));
            entries.add(new PieEntry(consumed - target, "초과"));
            colors.add(0xFF4CAF50); // green
            colors.add(0xFFF44336); // red
        }

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(colors);
        ds.setSliceSpace(2f); // 조각 간 간격
        ds.setValueTextSize(12f);
        ds.setValueTextColor(Color.BLACK);
        ds.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry entry) {
                return String.format("%.0f%s", value, unit);
            }
        });

        PieData data = new PieData(ds);
        chart.setData(data);

        // 중앙 텍스트 포맷팅
        String centerText = String.format(
                "%s\n%d / %d%s",
                title,
                (int) consumed,
                (int) target,
                unit
        );
        chart.setCenterText(centerText);

        chart.invalidate();
    }
}

