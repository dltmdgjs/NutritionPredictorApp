package com.seunghun.nutritionpredictorapp.ui.log;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.seunghun.nutritionpredictorapp.data.db.AppDbHelper;
import com.seunghun.nutritionpredictorapp.databinding.FragmentLogBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFragment extends Fragment {
    private FragmentLogBinding binding;
    AppDbHelper dbHelper;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LogViewModel logViewModel =
                new ViewModelProvider(this).get(LogViewModel.class);

        binding = FragmentLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // DBHelper 초기화
        dbHelper = new AppDbHelper(getContext());

        loadTodayIntakeInfo();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * DB에서 당일 섭취 기록을 읽어와 Text에 표시합니다.
     */
    private void loadTodayIntakeInfo() {
        // 오늘 날짜
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(date);

        // 오늘 날짜에 해당하는 섭취 기록을 불러옴
        SQLiteDatabase db1 = dbHelper.getReadableDatabase();
        Cursor cursor1 = db1.rawQuery("SELECT * FROM intake_log WHERE date = ?", new String[]{today});

        if (cursor1.moveToFirst()) {
            Toast.makeText(getContext(), "섭취 기록이 있습니다.", Toast.LENGTH_SHORT).show();
            String foods = "";
            int gram = 0;
            int calories = 0;
            int protein = 0;
            int carbohydrates = 0;
            int fats = 0;
            int fiber = 0;
            int sugars = 0;
            int sodium = 0;

            // 오늘 섭취한 영양 성분을 합함.
            int i = cursor1.getCount();
            for (int j = 0; j < i; j++) {
                String food = cursor1.getString(cursor1.getColumnIndexOrThrow("label"));
                foods += "\n" + food;
                gram += cursor1.getInt(cursor1.getColumnIndexOrThrow("grams"));
                calories += cursor1.getInt(cursor1.getColumnIndexOrThrow("calories"));
                protein += cursor1.getInt(cursor1.getColumnIndexOrThrow("protein"));
                carbohydrates += cursor1.getInt(cursor1.getColumnIndexOrThrow("carbs"));
                fats += cursor1.getInt(cursor1.getColumnIndexOrThrow("fats"));
                fiber += cursor1.getInt(cursor1.getColumnIndexOrThrow("fiber"));
                sugars += cursor1.getInt(cursor1.getColumnIndexOrThrow("sugars"));
                sodium += cursor1.getInt(cursor1.getColumnIndexOrThrow("sodium"));
                cursor1.moveToNext();
            }

            // 텍스트로 보여줌
            String str = "중량: " + gram + "g\n";
            str += "칼로리: " + calories + "kcal\n";
            str += "단백질: " + protein + "g\n";
            str += "탄수화물: " + carbohydrates + "g\n";
            str += "지방: " + fats + "g\n";
            str += "식이섬유: " + fiber + "g\n";
            str += "당류: " + sugars + "g\n";
            str += "나트륨: " + sodium + "mg\n";


            binding.tvLoadInfo.setText(str); // 섭취한 영양 성분 정보
            binding.tvTodayFood.append(foods); // 섭취한 음식명

            binding.tvTodayFood.setVisibility(View.VISIBLE);
            binding.tvTodayIntake.setVisibility(View.VISIBLE);

        } else {
            Toast.makeText(getContext(), "섭취 기록이 없습니다.", Toast.LENGTH_SHORT).show();
        }

        cursor1.close();
        db1.close();
    }
}