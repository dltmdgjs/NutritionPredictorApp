package com.seunghun.nutritionpredictorapp.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.seunghun.nutritionpredictorapp.data.LabelLoader;
import com.seunghun.nutritionpredictorapp.data.NutritionCalculator;
import com.seunghun.nutritionpredictorapp.data.db.AppDbHelper;
import com.seunghun.nutritionpredictorapp.databinding.FragmentHomeBinding;
import com.seunghun.nutritionpredictorapp.ml.FoodClassifier;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private FoodClassifier foodClassifier;
    private Bitmap selectedBitmap;
    private List<String> labels;
    private String label;
    private NutritionCalculator calc;
    private NutritionCalculator.NutritionInfo info;
    AppDbHelper dbHelper;

    // HomeFragment.java

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 시스템 바(상단바, 하단바)의 높이를 동적으로 계산하여 패딩 적용
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollRoot, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // 기존 XML의 padding 대신 시스템 바 높이만큼 정확히 여백 부여
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);

            return WindowInsetsCompat.CONSUMED;
        });
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dbHelper = new AppDbHelper(getContext());

        // 모델 로드
        foodClassifier = new FoodClassifier(requireContext());

        // 라벨 로드
        labels = LabelLoader.loadLabels(requireContext(), "labels.txt");

        // 음식 이미지 클릭 이벤트 리스너
        /** 이미지 클릭 시 카메라 접근해 이미지 변경하기 */
        binding.ivFood.setOnClickListener(v -> {
            Toast.makeText(getContext(), "카메라/갤러리 접근", Toast.LENGTH_SHORT).show();
            showImagePickerDialog();
        });

        // 영양정보 보기 버튼 클릭 이벤트 리스너
        /** 영양정보 보기 버튼 클릭 시, 예측 후 영양정보 창 전개 */
        binding.btPredict.setOnClickListener(v -> {
            // 이미지, 중량
            ImageView imageView = binding.ivFood;
            EditText editText = binding.etFweight;
            int gram;

            // 이미지 널체크
            if (imageView.getDrawable() == null) {
                Toast.makeText(getContext(), "이미지가 없습니다", Toast.LENGTH_SHORT).show();
                return;
            }
            // 중량 널체크
            if (editText.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "중량을 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            } else {
                gram = Integer.parseInt(editText.getText().toString());
            }

            // 예측 및 결과 보여주기
            predictAndShowResult(imageView, gram);
        });

        // 저장하기 버튼 클릭 이벤트 리스너
        /** 저장하기 버튼 클릭 시, 데이터 베이스에 저장 */
        binding.btSave.setOnClickListener(v -> {
            saveIntakeInfo();
        });

        // 중량 입력란 포커스시 키보드 올라오면 스크롤 올리기
        binding.etFweight.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {        // 키보드가 올라오는 시간을 고려해 약간의 지연 후 스크롤
                binding.getRoot().postDelayed(() -> {
                    binding.scrollRoot.smoothScrollTo(0, binding.etFweight.getBottom());
                }, 300);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (foodClassifier != null) {
            foodClassifier.close(); // 모델 닫기
        }
        binding = null;
    }

    /**
     * 예측 및 결과 보여주기 함수 (영양정보 보기 버튼 이벤트 클릭 리스너에서 실행됨)
     * @param imageView
     * @param gram
     */
    private void predictAndShowResult(ImageView imageView, int gram) {
        try {
            // ImageView → Bitmap 변환
            Bitmap bitmap = drawableToBitmap(imageView.getDrawable());

            // 예측 -> 결과 인덱스 반환
            int predictedIndex = foodClassifier.predict(bitmap);
//                Toast.makeText(getContext(), "예측 결과 클래스 인덱스: " + predictedIndex, Toast.LENGTH_LONG).show();

            // 인덱스 -> 라벨(음식명) 매핑
            label = labels.get(predictedIndex);
            Toast.makeText(getContext(), "음식명: " + label, Toast.LENGTH_SHORT).show();

            // 라벨(음식명) -> 영양정보 매핑(계산)
            calc = NutritionCalculator.fromAssets(requireContext(), "nutrition.csv");
            info = calc.getNutrition(label, gram);

            if (info == null) {
                Toast.makeText(getContext(), "영양정보 매칭 실패", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "영양정보 매칭 성공", Toast.LENGTH_SHORT).show();
                // tv_result에 매칭된 영양정보 표시.
                binding.tvResult.setText(
                        "음식명: " + label + "\n" +
                                "중량: " + info.weight + "g\n" +
                                "칼로리: " + info.calories + "kcal\n" +
                                "단백질: " + info.protein + "g\n" +
                                "탄수화물: " + info.carbohydrates + "g\n" +
                                "지방: " + info.fats + "g\n" +
                                "식이섬유: " + info.fiber + "g\n" +
                                "당류: " + info.sugars + "g\n" +
                                "나트륨: " + info.sodium + "mg"
                );
                binding.btSave.setVisibility(View.VISIBLE);

                binding.scrollRoot.post(() -> binding.scrollRoot.fullScroll(View.FOCUS_DOWN));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(
                    getContext(),
                    "추론 중 오류 발생",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    /**
     * 이미지 뷰를 비트맵으로 변환 함수 (predictAndShowResult 함수에서 실행됨)
     */
    private Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * 카메라/갤러리 접근 선택 다이얼로그 함수 (이미지 클릭 이벤트 리스너에서 실행됨)
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
                            binding.ivFood.setImageBitmap(bitmap);
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
                                binding.ivFood.setImageBitmap(selectedBitmap);
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
     * 데이터 베이스에 일자별 섭취 영양정보 저장 함수 (저장하기 버튼 이벤트 리스너에서 실행됨)
     */
    private void saveIntakeInfo() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(date);

        SQLiteDatabase db1 = dbHelper.getWritableDatabase();
        ContentValues values1 = new ContentValues();

        values1.put("date", today);
        values1.put("label", label);
        values1.put("grams", info.weight);
        values1.put("calories", info.calories);
        values1.put("protein", info.protein);
        values1.put("carbs", info.carbohydrates);
        values1.put("fats", info.fats);
        values1.put("fiber", info.fiber);
        values1.put("sugars", info.sugars);
        values1.put("sodium", info.sodium);
        db1.insert("intake_log", null, values1);
        Toast.makeText(getContext(), "섭취 정보가 저장되었습니다.", Toast.LENGTH_SHORT).show();
        db1.close();
    }
}