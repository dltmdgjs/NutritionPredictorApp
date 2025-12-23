package com.seunghun.nutritionpredictorapp.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.seunghun.nutritionpredictorapp.R;
import com.seunghun.nutritionpredictorapp.data.LabelLoader;
import com.seunghun.nutritionpredictorapp.data.NutritionCalculator;
import com.seunghun.nutritionpredictorapp.databinding.FragmentHomeBinding;
import com.seunghun.nutritionpredictorapp.ml.FoodClassifier;

import java.io.IOException;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private FoodClassifier foodClassifier;
    private Bitmap testBitmap;
    private Bitmap selectedBitmap;
    private List<String> labels;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 모델 로드
        foodClassifier = new FoodClassifier(requireContext());

        // 라벨 로드
        labels = LabelLoader.loadLabels(requireContext(), "labels.txt");


        // 테스트 이미지 표시
        testBitmap = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.food_test
        );
        binding.ivFood.setImageBitmap(testBitmap);


        // 음식 이미지 클릭 이벤트 리스너
        /** 이미지 클릭 시 카메라 접근해 이미지 변경하기 */
        binding.ivFood.setOnClickListener(v -> {
            Toast.makeText(getContext(), "카메라 접근", Toast.LENGTH_SHORT).show();
            //TODO: 카메라 접근 로직 구현 (완료)
            // 1. 사용자에 카메라 접근 요청
            // 2. 사진 촬영 (결과는 onActivityResult로 받음)
            // 3. 촬영한 사진을 ImageView에 표시

            // ----------------------------------------
            showImagePickerDialog();
        });


        // 영양정보 보기 버튼 클릭 이벤트 리스너
        /** 영양정보 보기 버튼 클릭 시, 예측 후 영양정보 창 전개 */
        binding.btPredict.setOnClickListener(v -> {
            // TODO: 예측 로직 구현
            //      - 파이썬 연동(모델 로드 및 예측 성공, FoodClassifier.java)
            //      - index -> label -> 영양정보 매핑(LabelLoader.java, NutritionCalculator.java)
            //TODO: 영양 정보 예측 창 전개 (현재 HomeFragment에 그대로 띄우기만 하고 있음)
            //      - 대안 : HomeFragment에 그대로 띄우되, 예측 성공 시 저장 버튼이 띄워지도록 함.

            // ------------------------------------------
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

            try {
                // ImageView → Bitmap 변환
                Bitmap bitmap = drawableToBitmap(imageView.getDrawable());

                // 예측 -> 결과 인덱스 반환
                int predictedIndex = foodClassifier.predict(bitmap);
//                Toast.makeText(getContext(), "예측 결과 클래스 인덱스: " + predictedIndex, Toast.LENGTH_LONG).show();

                // 인덱스 -> 라벨(음식명) 매핑
                String label = labels.get(predictedIndex);
                Toast.makeText(getContext(), "음식명: " + label, Toast.LENGTH_SHORT).show();

                // 라벨(음식명) -> 영양정보 매핑(계산)
                NutritionCalculator calc = NutritionCalculator.fromAssets(requireContext(), "nutrition.csv");
                NutritionCalculator.NutritionInfo info = calc.getNutrition(label, gram);

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
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(
                        getContext(),
                        "추론 중 오류 발생",
                        Toast.LENGTH_LONG
                ).show();
            }
        });


        return root;
    }

    // 이미지 뷰를 비트맵으로 변환
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (foodClassifier != null) {
            foodClassifier.close(); // 🔥 이 코드를 반드시 추가해야 합니다.
        }
        binding = null;
    }

    /* ===============================
       갤러리 런처
       =============================== */
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

    /* ===============================
       카메라 런처
       =============================== */
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



    /* ===============================
       이미지 선택 다이얼로그
       =============================== */
    private void showImagePickerDialog() {
        String[] options = {"카메라", "갤러리"};

        new AlertDialog.Builder(requireContext())
                .setTitle("이미지 선택")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    /* ===============================
       카메라
       =============================== */
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

    /* ===============================
       갤러리
       =============================== */
    private void openGallery() {
        galleryLauncher.launch("image/*");
    }
}