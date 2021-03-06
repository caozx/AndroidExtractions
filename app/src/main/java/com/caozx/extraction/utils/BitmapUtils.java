package com.caozx.extraction.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Czx on 2016/8/4.
 */
public class BitmapUtils {
    private static final String TAG = BitmapUtils.class.getCanonicalName();
    public static final String JPG_SUFFIX = ".jpg";
    private static final String TIME_FORMAT = "yyyyMMddHHmmss";

    public BitmapUtils() {
    }

    public static void displayToGallery(Context context, File photoFile) {
        if(photoFile != null && photoFile.exists()) {
            String photoPath = photoFile.getAbsolutePath();
            String photoName = photoFile.getName();

            try {
                ContentResolver e = context.getContentResolver();
                MediaStore.Images.Media.insertImage(e, photoPath, photoName, (String)null);
            } catch (FileNotFoundException var5) {
                var5.printStackTrace();
            }

            context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.parse("file://" + photoPath)));
        }
    }

    public static File saveToFile(Bitmap bitmap, File folder) {
        String fileName = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
        return saveToFile(bitmap, folder, fileName);
    }

    public static File saveToFile(Bitmap bitmap, File folder, String fileName) {
        if(bitmap != null) {
            if(!folder.exists()) {
                folder.mkdir();
            }

            File file = new File(folder, fileName + ".jpg");
            if(file.exists()) {
                file.delete();
            }

            try {
                file.createNewFile();
                BufferedOutputStream e = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, e);
                e.flush();
                e.close();
                return file;
            } catch (IOException var5) {
                var5.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public static int getBitmapDegree(String path) {
        short degree = 0;

        try {
            ExifInterface e = new ExifInterface(path);
            int orientation = e.getAttributeInt("Orientation", 1);
            switch(orientation) {
                case 3:
                    degree = 180;
                    break;
                case 6:
                    degree = 90;
                    break;
                case 8:
                    degree = 270;
            }
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        return degree;
    }

    public static Bitmap rotateBitmapByDegree(Bitmap bitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float)degree);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if(bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }

        return newBitmap;
    }

    public static Bitmap decodeBitmapFromFile(File imageFile, int requestWidth, int requestHeight) {
        return imageFile != null?decodeBitmapFromFile(imageFile.getAbsolutePath(), requestWidth, requestHeight):null;
    }

    public static Bitmap decodeBitmapFromFile(String imagePath, int requestWidth, int requestHeight) {
        if(TextUtils.isEmpty(imagePath)) {
            return null;
        } else {
            Log.i(TAG, "requestWidth: " + requestWidth);
            Log.i(TAG, "requestHeight: " + requestHeight);
            if(requestWidth > 0 && requestHeight > 0) {
                BitmapFactory.Options options1 = new BitmapFactory.Options();
                options1.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, options1);
                Log.i(TAG, "original height: " + options1.outHeight);
                Log.i(TAG, "original width: " + options1.outWidth);
                if(options1.outHeight == -1 || options1.outWidth == -1) {
                    try {
                        ExifInterface e = new ExifInterface(imagePath);
                        int height = e.getAttributeInt("ImageLength", 1);
                        int width = e.getAttributeInt("ImageWidth", 1);
                        Log.i(TAG, "exif height: " + height);
                        Log.i(TAG, "exif width: " + width);
                        options1.outWidth = width;
                        options1.outHeight = height;
                    } catch (IOException var7) {
                        var7.printStackTrace();
                    }
                }

                options1.inSampleSize = calculateInSampleSize(options1, requestWidth, requestHeight);
                Log.i(TAG, "inSampleSize: " + options1.inSampleSize);
                options1.inJustDecodeBounds = false;
                return BitmapFactory.decodeFile(imagePath, options1);
            } else {
                Bitmap options = BitmapFactory.decodeFile(imagePath);
                return options;
            }
        }
    }

    // Android 开发过程中，可能会用到的，通过一个uri获取一个Bitmap对象
    public static Bitmap decodeBitmapFromUri(Context context, Uri uri) {
        try {
            // 读取uri所在的图片
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }



    public static Bitmap decodeBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, (Rect)null, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, (Rect)null, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if(height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;

            for(int halfWidth = width / 2; halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth; inSampleSize *= 2) {
                ;
            }

            long totalPixels = (long)(width * height / inSampleSize);

            for(long totalReqPixelsCap = (long)(reqWidth * reqHeight * 2); totalPixels > totalReqPixelsCap; totalPixels /= 2L) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
