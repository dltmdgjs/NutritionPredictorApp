package com.seunghun.nutritionpredictorapp.ui.user;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.seunghun.nutritionpredictorapp.R;
import com.seunghun.nutritionpredictorapp.UserInfoDBHelper;
import com.seunghun.nutritionpredictorapp.databinding.FragmentUserBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;

// 사용자 정보 입력 및 저장을 위한 Fragment.
public class UserFragment extends Fragment {
    private FragmentUserBinding binding;
    UserInfoDBHelper mHelper;
    static final String mFILENAME = "myInfo.db";

    private EditText etName, etAge, etHeight, etWeight;
    private String gender = "남";
    private String activityLevel = "중";
    private Bitmap selectedBitmap = null;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        UserViewModel userViewModel =
                new ViewModelProvider(this).get(UserViewModel.class);

        binding = FragmentUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 위젯 초기화
        etName = binding.etName;
        etAge = binding.etAge;
        etHeight = binding.etHeight;
        etWeight = binding.etWeight;


        // DBHelper 초기화
        mHelper = new UserInfoDBHelper(getContext(), mFILENAME, null, 1);

        // 1. 화면 로드 시 데이터 조회 및 표시
        restoreProfileImage(); // 프로필 이미지 로드
        loadUserData(); // 사용자 정보 데이터 로드

        /** 프로필 이미지 클릭 시 사진첩 접근해 이미지 변경하기 */
        binding.ivProfile.setOnClickListener(v -> {
            showImagePickerDialog(); // selectedBitmap 업데이트
        });

        /** 성별 라디오 버튼 */
        binding.rgGender.setOnCheckedChangeListener((group, checkedId)->
                {
                    if (checkedId == binding.rbMan.getId()) {
                        gender = "남";
                    }
                    else if (checkedId == binding.rbWoman.getId()) {
                        gender = "여";
                    }
                }
        );

        /** 활동량 라디오 버튼 */
        binding.rgActivityLevel.setOnCheckedChangeListener((group, checkedId)-> {
                    if (checkedId == binding.rbActivityLevelLow.getId()) {
                        activityLevel = "저";
                    }
                    else if (checkedId == binding.rbActivityLevelMedium.getId()) {
                        activityLevel = "중";
                    }
                    else if (checkedId == binding.rbActivityLevelHigh.getId()) {
                        activityLevel = "고";
                    }
                }
        );

        /** 저장 버튼 리스너 설정 */
        binding.btSave.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String age = etAge.getText().toString();
            String height = etHeight.getText().toString();
            String weight = etWeight.getText().toString();

            if (name.isEmpty() || age.isEmpty() || height.isEmpty() || weight.isEmpty()) {
                Toast.makeText(getContext(), "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. 데이터 저장 또는 업데이트 로직 실행
            saveOrUpdateProfile(name, age, height, weight, gender, activityLevel);
        });

        return root;
    }

    /**
     * DB에서 사용자 정보를 읽어와 EditText에 표시합니다.
     * DB 작업이 끝나면 리소스를 항상 닫습니다.
     */
    private void loadUserData() {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, age, height, weight, gender, activity FROM myinfo", null);

        // moveToFirst()는 커서를 첫 번째 행으로 이동시키며, 데이터가 있으면 true를 반환합니다.
        if (cursor.moveToFirst()) {
            // getColumnIndexOrThrow는 컬럼이 없을 경우 예외를 발생시켜 실수를 방지합니다.
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            etAge.setText(cursor.getString(cursor.getColumnIndexOrThrow("age")));
            etHeight.setText(cursor.getString(cursor.getColumnIndexOrThrow("height")));
            etWeight.setText(cursor.getString(cursor.getColumnIndexOrThrow("weight")));
            gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"));
            activityLevel = cursor.getString(cursor.getColumnIndexOrThrow("activity"));
            if (gender.equals("남")) {
                binding.rbMan.setChecked(true);
            } else {
                binding.rbWoman.setChecked(true);
            }
            if (activityLevel.equals("저")) {
                binding.rbActivityLevelLow.setChecked(true);
            } else if (activityLevel.equals("중")) {
                binding.rbActivityLevelMedium.setChecked(true);
            } else {
                binding.rbActivityLevelHigh.setChecked(true);
            }
        }

        // 리소스 누수 방지를 위해 Cursor와 DB를 반드시 닫아줍니다.
        cursor.close();
        db.close();
    }

    /**
     * 데이터 존재 여부에 따라 정보를 저장(INSERT)하거나 업데이트(UPDATE)합니다.
     * 안전한 ContentValues를 사용하여 SQL Injection을 방지합니다.
     */
    private void saveOrUpdateProfile(String name, String age, String height, String weight, String gender, String activityLevel) {
        if (selectedBitmap != null) {
            persistProfile(selectedBitmap); // 프로필 사진 저장
        }

        SQLiteDatabase db = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("age", age);
        values.put("height", height);
        values.put("weight", weight);
        values.put("gender", gender);
        values.put("activity", activityLevel);

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

    /**
     * 카메라/갤러리 접근 선택 다이얼로그 함수 (프로필 이미지 선택)
     */
    private void showImagePickerDialog() {
        String[] options = {"카메라", "갤러리"};

        new AlertDialog.Builder(requireContext())
                .setTitle("이미지 선택")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera(); // 카메라 열기
                    } else {
                        openGallery(); // 갤러리 열기
                    }
                })
                .show();
    }

    // 카메라 런처
    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicturePreview(),
                    bitmap -> {
                        if (bitmap != null) {
                            selectedBitmap = bitmap;
                            binding.ivProfile.setImageBitmap(bitmap);
                        }
                    }
            );

    // 갤러리 런처
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            try {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(
                                        requireActivity().getContentResolver(), uri
                                );
                                binding.ivProfile.setImageBitmap(selectedBitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );

    // 카메라 열기 함수
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    1001
            );
        } else {
            cameraLauncher.launch(null);
        }
    }

    // 갤러리 열기 함수
    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    /**
     * 프로필 이미지 로드 함수
     */
    private void restoreProfileImage() {
        String path = ProfilePrefs.getProfilePath(requireContext());
        if (path == null) return;

        File f = new File(path);
        if (!f.exists()) return;

        binding.ivProfile.setImageBitmap(BitmapFactory.decodeFile(path));
    }

    /**
     * 프로필 이미지 저장 함수
     */
    private void persistProfile(Bitmap selected) {
        if (selected == null) {
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String path = ProfileImageStore.saveProfileBitmap(requireContext(), selected);
                ProfilePrefs.saveProfilePath(requireContext(), path);

                requireActivity().runOnUiThread(() -> {
                    // Toast.makeText(getContext(), "프로필 저장됨", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    // Toast.makeText(getContext(), "프로필 저장 실패", Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
