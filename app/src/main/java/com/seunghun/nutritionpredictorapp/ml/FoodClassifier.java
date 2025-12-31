package com.seunghun.nutritionpredictorapp.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FoodClassifier {

    private static final int IMG_SIZE = 224;
    private static final int NUM_CLASSES = 101;

    private Interpreter interpreter;

    // ===============================
    // 생성자: 모델 로드
    // ===============================
    public FoodClassifier(Context context) {
        try {
            interpreter = new Interpreter(loadModel(context), getOptions());
        } catch (Exception e) {
            throw new RuntimeException("TFLite 모델 로드 실패", e);
        }
    }

    // ===============================
    // 추론 함수 (Bitmap → class index)
    // ===============================
    public int predict(Bitmap bitmap) {

        // 1) 리사이즈만 하고, 정규화(나누기 255 / -1~1)는 하지 않습니다.
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(IMG_SIZE, IMG_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                // .add(new NormalizeOp(...))  // 절대 넣지 마세요 (raw255 모델)
                .build();

        // 2) 모델 입력: FLOAT32, 값 범위는 0~255
        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(bitmap);
        tensorImage = imageProcessor.process(tensorImage);

        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(
                new int[]{1, NUM_CLASSES},
                DataType.FLOAT32
        );

        if (interpreter != null) {
            interpreter.run(tensorImage.getBuffer(), outputBuffer.getBuffer().rewind());
        }

        float[] probabilities = outputBuffer.getFloatArray();
        int maxIndex = -1;
        float maxProb = -1f;
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i];
                maxIndex = i;
            }
        }

        Log.d("FoodClassifier", "Max Probability: " + maxProb + " at Index: " + maxIndex);
        return maxIndex;
    }

    // ===============================
    // TFLite 옵션 (CPU only)
    // ===============================
    private Interpreter.Options getOptions() {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        return options;
    }

    // ===============================
    // 모델 로딩
    // ===============================
    private MappedByteBuffer loadModel(Context context) throws Exception {

        FileInputStream fis = new FileInputStream(
                context.getAssets()
                        .openFd("food101_mnv3large_fp32.tflite")
                        .getFileDescriptor()
        );

        FileChannel channel = fis.getChannel();

        long startOffset = context.getAssets()
                .openFd("food101_mnv3large_fp32.tflite")
                .getStartOffset();

        long declaredLength = context.getAssets()
                .openFd("food101_mnv3large_fp32.tflite")
                .getDeclaredLength();

        return channel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
        );
    }

    // 모델 닫기. 초기화
    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
            Log.d("FoodClassifier", "TFLite interpreter is closed.");
        }
    }
}

