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

        loadData();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadData() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(date);

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

            String str = "중량: " + gram + "g\n";
            str += "칼로리: " + calories + "kcal\n";
            str += "단백질: " + protein + "g\n";
            str += "탄수화물: " + carbohydrates + "g\n";
            str += "지방: " + fats + "g\n";
            str += "식이섬유: " + fiber + "g\n";
            str += "당류: " + sugars + "g\n";
            str += "나트륨: " + sodium + "mg\n";


            binding.tvLoadInfo.setText(str);
            binding.tvFoodIntake.append(foods);
            binding.tvFoodIntake.setVisibility(View.VISIBLE);
            binding.textView4.setVisibility(View.VISIBLE);

        } else {
            Toast.makeText(getContext(), "섭취 기록이 없습니다.", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
        db.close();
    }
}