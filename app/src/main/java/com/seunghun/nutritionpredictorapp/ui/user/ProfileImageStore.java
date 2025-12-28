package com.seunghun.nutritionpredictorapp.ui.user;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;

public class ProfileImageStore {
    private static final String DIR_NAME = "profile";
    private static final String FILE_NAME = "profile.png";

    /**
     * 프로필 사진 저장 (path반환)
     */
    public static String saveProfileBitmap(Context context, Bitmap bitmap) throws Exception {
        File dir = new File(context.getFilesDir(), DIR_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File outFile = new File(dir, FILE_NAME);
        try (FileOutputStream fos = new FileOutputStream(outFile, false)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        }
        return outFile.getAbsolutePath();
    }

    /**
     * 프로필 사진 파일 불러오기
     */
    public static File getProfileFile(Context context) {
        return new File(new File(context.getFilesDir(), DIR_NAME), FILE_NAME);
    }
}
