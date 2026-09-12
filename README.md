# NutritionPredictorApp (Android Java)
음식 사진을 분류(Food-101)한 뒤, `nutrition.csv`와 매핑하여 영양성분을 계산하고  
일자별 섭취 기록을 SQLite에 저장한 다음, 달력/파이차트로 “권장 섭취량 대비 실섭취량”을 시각화하는 앱입니다.

---

<img src="https://github.com/user-attachments/assets/52d256ea-45be-4a67-8c65-044bcae3c42b" width="300" height="700" />

---
### 프론트엔드 수정 (25/01/15)
<img width="300" height="700" alt="Image" src="https://github.com/user-attachments/assets/6fee0a8b-216a-4533-9cb1-93b8f5331ca0" />

<img width="300" height="700" alt="Image" src="https://github.com/user-attachments/assets/3f99abcd-3145-4c1e-85df-b0089bbcb817" />

<img width="300" height="700" alt="Image" src="https://github.com/user-attachments/assets/64efb031-760d-4e25-8ec2-0b21ae988cc9" />

<img width="300" height="700" alt="Image" src="https://github.com/user-attachments/assets/df8ea014-9441-4e9f-8613-136915e55ba8" />

<img width="300" height="700" alt="Image" src="https://github.com/user-attachments/assets/326e2f49-bfcc-424b-885f-1019a60f7353" />

---

## 1. 주요 기능
### 1) 음식 이미지 분류 (On-device)
- 카메라 촬영 / 갤러리 선택 → `ImageView` 표시
- TensorFlow Lite 모델로 음식 101종 분류 (Food-101)
- 분류 결과 Top-1(Label) 확정

### 2) 영양정보 계산 및 기록 (SQLite)
- `nutrition.csv` 구조: (label, weight, calories, protein, carbs, fats, fiber, sugars, sodium …)
- 사용자가 입력한 중량(g)에 대해:
  - **정확히 동일한 weight가 있으면 해당 row 사용**
  - 범위 밖이면 **비율(ratio) 스케일링**
  - 범위 안이면 **인접 weight 사이 선형 보간(linear interpolation)**
- 계산 결과를 `int`로 반올림하여 저장/표시

### 3) 달력(Calendar) + 일자 상세(Day Detail)
- 달력에 날짜별로 섭취한 음식명 요약 표시
- 날짜 클릭 → Day Detail 화면으로 이동
  - 해당 날짜 섭취 음식명 리스트
  - 영양 총합(칼로리/단백질/탄수화물/지방/식이섬유/당류/나트륨)
  - 사용자 신체정보(나이/키/몸무게/성별/활동량) 기반 목표치 대비 파이차트 표시(MPAndroidChart)

---

## 2. 사용 기술
- Android: Java, Fragment, ViewBinding
- DB: SQLite
- ML Inference: TensorFlow Lite (`Interpreter`, `tflite-support` ImageProcessor)
- Chart: MPAndroidChart (`com.github.PhilJay:MPAndroidChart`)
- Calendar: Kizitonwose CalendarView
- Training: Google Colab (A100) + TensorFlow/Keras

---

## 3. 모델 학습 결과 (Food-101)
- Backbone: MobileNetV3Large (ImageNet pretrained)
- 입력 크기: `IMG_SIZE = 224`
- 학습 로그 예시:
  - Train accuracy 약 0.96
  - Validation accuracy 약 **0.79~0.795** 수준까지 수렴

> 핵심: 학습은 “정상적으로 됐는데” 추론에서 항상 같은 클래스(예: 53)만 나오던 문제는  
> **전처리 스케일 불일치(0~255 vs 0~1 vs [-1,1])** 때문에 발생했음.  
> 전처리를 학습과 추론에서 완전히 동일하게 맞추면 정상 동작함.

---

## 4. 전처리(Preprocess) 주의사항
모델이 기대하는 입력 스케일이 학습과 추론에서 다르면,
- 서로 다른 이미지를 넣어도 Top-1이 한 클래스로 고정되는 현상이 발생할 수 있습니다.

### 1) 대표적인 스케일 케이스
- `raw255`: 입력이 0~255 범위
- `float01`: 입력이 0~1 범위
- `m11`: MobileNet 계열에서 흔한 [-1, 1] 범위 (`x/127.5 - 1`)

### 2) Android(TFLite)에서의 예
- 학습에서 MobileNetV3 `preprocess_input`(대개 [-1,1])을 썼다면:
  - Android에서 `NormalizeOp(127.5f, 127.5f)` (즉, (x-127.5)/127.5)로 맞추는 방식이 일반적
- 학습에서 단순 `x/255.0`(0~1)로 맞췄다면:
  - Android에서 `NormalizeOp(0.0f, 255.0f)`로 맞춤

---

## 5. TFLite 변환
### 1) 권장
- 학습 완료 후 `.keras`로 베스트 체크포인트 저장
- 추론용 모델만 별도로 export(SavedModel)한 뒤 TFLite 변환

### 2) Android 호환 팁
- 가능한 `SELECT_TF_OPS(Flex)` 없이 **TFLITE_BUILTINS만**으로 변환/사용 권장  
  (`FlexMul` 같은 에러를 피하기 위해)
- 부득이하게 `SELECT_TF_OPS`가 들어가면:
  - Android Gradle에 `org.tensorflow:tensorflow-lite-select-tf-ops` 추가 필요

---

## 6. Android 프로젝트 구조 가이드
### 1) assets
- `app/src/main/assets/`
  - `model.tflite` (예: `food101_mnv3large_fp32.tflite`)
  - `labels.txt` (Food-101 label index 순서 파일)
  - `nutrition.csv`

### 2) Gradle 설정(요지)
- `.tflite` 압축 방지(noCompress) 설정
- TFLite + support 라이브러리 의존성 추가

---

## 7. 동작 흐름(앱)
1) 카메라/갤러리로 이미지 가져오기 → `ImageView.setImageBitmap()`
2) `FoodClassifier.predict(bitmap)` → 예측 class index
3) index → label 매핑(labels.txt)
4) label + 입력 중량(g) → `nutrition.csv`에서 영양 계산
5) (date, label, grams, calories, ...) SQLite 저장
6) Calendar에서 월별 섭취 음식 표시
7) 날짜 클릭 → DayDetail에서 총합 + 파이차트 표시

---

## 8. 향후 개선 아이디어
- 기록 삭제/수정 기능 추가
- 영양 목표치 정책을 사용자 설정(감량/유지/증량)으로 확장
- Room ORM로 마이그레이션(선택)
- UI 개선
- AI 기반 추천 식단 제공 기능 추가
- 더 많은 음식 클래스 제공

---

## References / Open Source Licenses

This project uses the following open-source libraries and frameworks.  
Please refer to each project’s repository/license for full terms.

- **MPAndroidChart** (PieChart UI) — Apache License 2.0  
  https://github.com/PhilJay/MPAndroidChart

- **Kizitonwose CalendarView** (Calendar UI) — MIT License  
  https://github.com/kizitonwose/Calendar

- **TensorFlow Lite** (On-device inference) — Apache License 2.0  
  https://www.tensorflow.org/lite

- **TensorFlow Lite Support Library** (Image processing utilities) — Apache License 2.0  
  https://github.com/tensorflow/tflite-support

- **Material Components for Android** (UI components) — Apache License 2.0  
  https://github.com/material-components/material-components-android

- **AndroidX Libraries** (AppCompat, Fragment, Lifecycle, etc.) — Apache License 2.0  
  https://developer.android.com/jetpack/androidx

---

## License
- MIT License
