package com.seunghun.nutritionpredictorapp.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.seunghun.nutritionpredictorapp.R;
import com.seunghun.nutritionpredictorapp.data.NutritionRepository;
import com.seunghun.nutritionpredictorapp.databinding.FragmentHomeBinding;
import com.seunghun.nutritionpredictorapp.ml.FoodClassifier;

import java.io.IOException;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private FoodClassifier foodClassifier;
    private Bitmap testBitmap;
    private Bitmap selectedBitmap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 모델 로드
        foodClassifier = new FoodClassifier(requireContext());

        // 테스트 이미지 표시
        testBitmap = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.food_test
        );
        binding.ivFood.setImageBitmap(testBitmap);

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

        /** 영양정보 보기 버튼 리스너 설정 */
        binding.btPredict.setOnClickListener(v -> {
            Toast.makeText(getContext(), "영양정보 보기", Toast.LENGTH_SHORT).show();
            //TODO: 영양 정보 예측 창 전개
            // 1. 예측 로직 구현
            //      - 파이썬 연동(모델 로드 및 예측 성공, FoodClassifier.java)
            //      - 음식 DB 연동(해야함, NutritionRepository.java)
            // 일단 모델을 불러와 예측해, 클래스 인덱스를 도출하는 것까지 진행됨.
            // 다음으로는 인덱스를 음식명으로 전환하고, 이를 통해 영양성분을 얻어 띄우는 것을 해야함.

            // ------------------------------------------
            ImageView imageView = binding.ivFood;

            if (imageView.getDrawable() == null) {
                Toast.makeText(getContext(), "이미지가 없습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // 🔥 ImageView → Bitmap 변환
                Bitmap bitmap = drawableToBitmap(imageView.getDrawable());

                int predictedIndex = foodClassifier.predict(bitmap);

                Toast.makeText(
                        getContext(),
                        "예측 결과 클래스 인덱스: " + predictedIndex,
                        Toast.LENGTH_LONG
                ).show();

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

    // 이미지뷰를 비트맵으로 변환
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