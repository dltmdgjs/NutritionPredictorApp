package com.seunghun.nutritionpredictorapp.ui.user;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.seunghun.nutritionpredictorapp.ContactsDBHelper;
import com.seunghun.nutritionpredictorapp.databinding.FragmentUserBinding;

// 사용자 정보 입력 및 저장을 위한 Fragment
public class UserFragment extends Fragment {

    ContactsDBHelper mHelper;
    static final String mFILENAME = "myInfo.db";
    private FragmentUserBinding binding;

    private EditText etName, etAge, etHeight, etWeight;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        UserViewModel dashboardViewModel =
                new ViewModelProvider(this).get(UserViewModel.class);

        binding = FragmentUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 위젯 초기화
        etName = binding.etName;
        etAge = binding.etAge;
        etHeight = binding.etHeight;
        etWeight = binding.etWeight;

        // DBHelper 초기화
        mHelper = new ContactsDBHelper(getContext(), mFILENAME, null, 1);

        // 1. 화면 로드 시 데이터 조회 및 표시
        loadData();

        // 저장 버튼 리스너 설정
        Button btSave = binding.btSave;
        btSave.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String age = etAge.getText().toString();
            String height = etHeight.getText().toString();
            String weight = etWeight.getText().toString();

            if (name.isEmpty() || age.isEmpty() || height.isEmpty() || weight.isEmpty()) {
                Toast.makeText(getContext(), "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. 데이터 저장 또는 업데이트 로직 실행
            saveOrUpdateData(name, age, height, weight);
        });

        return root;
    }

    /**
     * DB에서 사용자 정보를 읽어와 EditText에 표시합니다.
     * DB 작업이 끝나면 리소스를 항상 닫습니다.
     */
    private void loadData() {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, age, height, weight FROM myinfo", null);

        // moveToFirst()는 커서를 첫 번째 행으로 이동시키며, 데이터가 있으면 true를 반환합니다.
        if (cursor.moveToFirst()) {
            // getColumnIndexOrThrow는 컬럼이 없을 경우 예외를 발생시켜 실수를 방지합니다.
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            etAge.setText(cursor.getString(cursor.getColumnIndexOrThrow("age")));
            etHeight.setText(cursor.getString(cursor.getColumnIndexOrThrow("height")));
            etWeight.setText(cursor.getString(cursor.getColumnIndexOrThrow("weight")));
        }

        // 리소스 누수 방지를 위해 Cursor와 DB를 반드시 닫아줍니다.
        cursor.close();
        db.close();
    }

    /**
     * 데이터 존재 여부에 따라 정보를 저장(INSERT)하거나 업데이트(UPDATE)합니다.
     * 안전한 ContentValues를 사용하여 SQL Injection을 방지합니다.
     */
    private void saveOrUpdateData(String name, String age, String height, String weight) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("age", age);
        values.put("height", height);
        values.put("weight", weight);

        // 테이블의 데이터 개수를 확인하여 분기 처리
        long count = db.compileStatement("SELECT COUNT(*) FROM myinfo").simpleQueryForLong();

        if (count > 0) {
            // 데이터가 있으면 UPDATE
            // WHERE 절을 생략하면 모든 행이 업데이트됩니다. 이 앱에서는 데이터가 하나이므로 괜찮습니다.
            db.update("myinfo", values, null, null);
            Toast.makeText(getContext(), "정보가 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            // 데이터가 없으면 INSERT
            db.insert("myinfo", null, values);
            Toast.makeText(getContext(), "정보가 저장되었습니다.", Toast.LENGTH_SHORT).show();
        }

        // DB 사용 후 닫기
        db.close();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
