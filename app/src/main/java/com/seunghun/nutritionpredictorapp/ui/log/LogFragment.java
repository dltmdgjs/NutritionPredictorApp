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

import com.seunghun.nutritionpredictorapp.IntakeInfoDBHelper;
import com.seunghun.nutritionpredictorapp.databinding.FragmentLogBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFragment extends Fragment {
    private FragmentLogBinding binding;
    IntakeInfoDBHelper mHelper;
    static final String mFILENAME = "intakeInfo.db";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LogViewModel logViewModel =
                new ViewModelProvider(this).get(LogViewModel.class);

        binding = FragmentLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // DBHelper 초기화
        mHelper = new IntakeInfoDBHelper(getContext(), mFILENAME, null, 1);

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
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM intakeinfo WHERE date = ?", new String[]{today});

        if (cursor.moveToFirst()) {
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
            int i = cursor.getCount();
            for (int j = 0; j < i; j++) {
                String food = cursor.getString(cursor.getColumnIndexOrThrow("food"));
                foods += "\n" + food;
                gram += cursor.getInt(cursor.getColumnIndexOrThrow("gram"));
                calories += cursor.getInt(cursor.getColumnIndexOrThrow("calories"));
                protein += cursor.getInt(cursor.getColumnIndexOrThrow("protein"));
                carbohydrates += cursor.getInt(cursor.getColumnIndexOrThrow("carbohydrates"));
                fats += cursor.getInt(cursor.getColumnIndexOrThrow("fats"));
                fiber += cursor.getInt(cursor.getColumnIndexOrThrow("fiber"));
                sugars += cursor.getInt(cursor.getColumnIndexOrThrow("sugars"));
                sodium += cursor.getInt(cursor.getColumnIndexOrThrow("sodium"));
                cursor.moveToNext();
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

        cursor.close();
        db.close();
    }
}