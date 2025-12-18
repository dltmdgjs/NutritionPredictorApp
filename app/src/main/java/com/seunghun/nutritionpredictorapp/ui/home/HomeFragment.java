package com.seunghun.nutritionpredictorapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.seunghun.nutritionpredictorapp.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {


    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        /** 이미지 클릭 시 카메라 접근해 이미지 변경하기 */
        ImageView imageView = binding.ivFood;
        imageView.setOnClickListener(v -> {
            Toast.makeText(getContext(), "카메라 접근", Toast.LENGTH_SHORT).show();
            //TODO: 카메라 접근 로직 구현
            // 1. 사용자에 카메라 접근 요청
            // 2. 사진 촬영 (결과는 onActivityResult로 받음)
            // 3. 촬영한 사진을 ImageView에 표시
        });

        /** 영양정보 보기 버튼 리스너 설정 */
        Button btPredict = binding.btPredict;
        btPredict.setOnClickListener(v -> {
            Toast.makeText(getContext(), "영양정보 보기", Toast.LENGTH_SHORT).show();
            // 영양 정보 예측 창 띄우기 (이미지와 중량 정보를 넘겨야 함.)
            // TODO: 영양 정보 예측 창 전개
            // TODO: 예측 로직 구현 - 파이썬 연동, 음식 DB 연동
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}