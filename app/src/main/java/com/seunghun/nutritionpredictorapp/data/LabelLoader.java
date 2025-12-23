package com.seunghun.nutritionpredictorapp.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

// 모델을 통해 추론한 결과로 얻은 인덱스를 음식명으로 매핑.
public class LabelLoader {
    private LabelLoader() {
    }

    public static List<String> loadLabels(Context context, String assetFileName) {
        List<String> labels = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(assetFileName))
        )) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    labels.add(line);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("labels.txt 로드 실패: " + assetFileName, e);
        }
        return labels;
    }
}
