package com.caozx.extraction.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.caozx.extraction.helper.CapturePhotoHelper;
import com.caozx.extraction.manager.FolderManager;
import com.caozx.extraction.R;
import com.caozx.extraction.utils.BitmapUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 封装（调用系统相机拍照或图库选择、裁剪并保存）功能
 * <p/>
 * 注：其中调用系统相机拍照功能因适配多机型，使用“D_clock爱吃葱花”解决方案
 * 对该模块感兴趣者可访问其GitHub：https://github.com/D-clock/AndroidStudyCode
 *
 * @author Czx
 * @time 2016/8/4
 */

public class PhotographActivity extends Activity implements View.OnClickListener {
    private final static String TAG = PhotographActivity.class.getSimpleName();

    private final static String EXTRA_RESTORE_PHOTO = "extra_restore_photo";
    private final static String TIMESTAMP_FORMAT = "yyyy_MM_dd_HH_mm_ss";

    private final static int RUNTIME_PERMISSION_REQUEST_CODE = 0x0;//运行时权限申请码
    private static final int CAPTURE_PIC_BY_PHOTO_TAKE = 0x1;// 拍照获取图片
    private static final int CAPTURE_PIC_BY_PHOTO_ALBUM = 0x2;// 相册中的图片
    private static final int CAPTURE_PIC_TO_CROP = 0x3;// 裁剪

    public static final String IS_CROP = "isCrop";// 是否需要裁剪
    public static final String CROP_OUTPUT_X = "outputX";// 只有在裁剪状态才有效,裁剪输出的x
    public static final String CROP_OUTPUT_Y = "outputY";// 只有在裁剪状态才有效,裁剪输出的y

    private String picName;
    private boolean isCrop;
    private int outputX, outputY;

    private CapturePhotoHelper mCapturePhotoHelper;
    private File mRestorePhotoFile;

    // 最终的图片资源预览
    private ImageView ivPic;

    private View vBtnAll;

    public static void start(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, PhotographActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void start(Activity activity, int requestCode, boolean isCrop, int outputX, int outputY) {
        Intent intent = new Intent(activity, PhotographActivity.class);
        intent.putExtra(IS_CROP, isCrop);
        intent.putExtra(CROP_OUTPUT_X, outputX);
        intent.putExtra(CROP_OUTPUT_Y, outputY);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photograph);

        Intent intent = getIntent();
        isCrop = intent.getBooleanExtra(IS_CROP, true);
        outputX = intent.getIntExtra(CROP_OUTPUT_X, 300);
        outputY = intent.getIntExtra(CROP_OUTPUT_Y, 300);
        picName = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());

        findViewById(R.id.rl_capture_pic_finish).setOnClickListener(this);
        findViewById(R.id.btn_capture_pic_by_album).setOnClickListener(this);
        findViewById(R.id.btn_capture_pic_by_photo).setOnClickListener(this);
        ivPic = (ImageView) findViewById(R.id.iv_capture_pic_img);
        vBtnAll = findViewById(R.id.ll_capture_pic_btn_all);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_capture_pic_by_album:// 相册选择
                Intent choosePictureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(choosePictureIntent, CAPTURE_PIC_BY_PHOTO_ALBUM);
                break;
            case R.id.btn_capture_pic_by_photo:// 拍照
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //Android M 处理Runtime Permission
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {//检查是否有写入SD卡的授权
                        Log.i(TAG, "granted permission!");
                        turnOnCamera();
                    } else {
                        Log.i(TAG, "denied permission!");
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            Log.i(TAG, "should show request permission rationale!");
                        }
                        requestPermission();
                    }
                } else {
                    turnOnCamera();
                }
                break;
            default:
                finish();
                break;
        }
        vBtnAll.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        if (mCapturePhotoHelper != null) {
            mRestorePhotoFile = mCapturePhotoHelper.getPhoto();
            Log.i(TAG, "onSaveInstanceState , mRestorePhotoFile: " + mRestorePhotoFile);
            if (mRestorePhotoFile != null) {
                outState.putSerializable(EXTRA_RESTORE_PHOTO, mRestorePhotoFile);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        if (mCapturePhotoHelper != null) {
            mRestorePhotoFile = (File) savedInstanceState.getSerializable(EXTRA_RESTORE_PHOTO);
            Log.i(TAG, "onRestoreInstanceState , mRestorePhotoFile: " + mRestorePhotoFile);
            mCapturePhotoHelper.setPhoto(mRestorePhotoFile);
        }
    }

    /**
     * 开启相机
     */
    private void turnOnCamera() {
        if (mCapturePhotoHelper == null) {
            mCapturePhotoHelper = new CapturePhotoHelper(this, FolderManager.getPicFolder());
        }
        mCapturePhotoHelper.capture(CAPTURE_PIC_BY_PHOTO_TAKE, picName);
    }


    /**
     * 申请写入sd卡的权限
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RUNTIME_PERMISSION_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RUNTIME_PERMISSION_REQUEST_CODE) {
            for (int index = 0; index < permissions.length; index++) {
                String permission = permissions[index];
                if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                    if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG, "onRequestPermissionsResult: permission is granted!");
                        turnOnCamera();
                    } else {
                        showMissingPermissionDialog();
                    }
                }
            }
        }
    }

    /**
     * 显示打开权限提示的对话框
     */
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.help);
        builder.setMessage(R.string.help_content);

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(PhotographActivity.this, R.string.camera_open_error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        builder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                turnOnSettings();
            }
        });

        builder.show();
    }

    /**
     * 启动系统权限设置界面
     */
    private void turnOnSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    /**
     * 裁剪图片方法实现 图片裁剪比例1:1 保存宽高330x300
     */
    public void beginCrop(Uri uri) {
        //根据宽高计算宽高比例
        int aspectX = 1, aspectY = 1;
        if (outputX != 0 && outputY != 0) {
            int maxCommonDivisor = maxCommonDivisor(outputX, outputY);
            aspectX = outputX / maxCommonDivisor;
            aspectY = outputY / maxCommonDivisor;
        }

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        // outputX outputY
        // 是裁剪图片宽高，注意如果return-data=true情况下,其实得到的是缩略图，并不是真实拍摄的图片大小，
        // 而原因是拍照的图片太大，所以这个宽高当你设置很大的时候发现并不起作用，就是因为返回的原图是缩略图，但是作为头像还是够清晰了
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        // 返回图片数据
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CAPTURE_PIC_TO_CROP);
    }

    private int maxCommonDivisor(int m, int n) {
        if (m < n) {// 保证m>n,若m<n,则进行数据交换
            int temp = m;
            m = n;
            n = temp;
        }
        if (m % n == 0) {// 若余数为0,返回最大公约数
            return n;
        } else { // 否则,进行递归,把n赋给m,把余数赋给n
            return maxCommonDivisor(n, m % n);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "requestCode: " + requestCode + " resultCode: " + resultCode + " data: " + data);
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CAPTURE_PIC_BY_PHOTO_TAKE://拍照结果
                File photoFile = mCapturePhotoHelper.getPhoto();
                if (photoFile != null) {
                    if (resultCode == RESULT_OK) {
                        int degree = BitmapUtils.getBitmapDegree(photoFile.getAbsolutePath());//检查是否有被旋转，并进行纠正
                        if (degree != 0) {
                            Bitmap bitmap = BitmapUtils.rotateBitmapByDegree(BitmapUtils.decodeBitmapFromFile(photoFile, 0, 0), degree);
                            if (isCrop) {
                                beginCrop(Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null, null)));
                            } else {
                                successFinsh(bitmap);
                            }
                        } else {
                            if (isCrop) {
                                beginCrop(Uri.fromFile(photoFile));
                            } else {
                                //此时图片已本地生成，不需做其他处理
                                successFinsh(null);
                            }
                        }
                    } else {
                        if (photoFile.exists()) {
                            photoFile.delete();
                        }
                        finish();
                    }
                }
                break;
            case CAPTURE_PIC_BY_PHOTO_ALBUM: // 选择图库图片结果
                if (resultCode == RESULT_OK) {
                    if (isCrop) {
                        beginCrop(data.getData());
                    } else {
                        successFinsh(BitmapUtils.decodeBitmapFromUri(this, data.getData()));
                    }
                } else {
                    finish();
                }

                break;
            case CAPTURE_PIC_TO_CROP://裁剪结果
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap photo = extras.getParcelable("data");
                        /** 本地保存裁剪后图片 */
                        successFinsh(photo);
                    }
                } else {
                    finish();
                }
                break;
        }
    }


    private void successFinsh(Bitmap bitmap) {
        if (bitmap != null) {
            BitmapUtils.saveToFile(bitmap, FolderManager.getPicFolder(), picName);
        }
        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
//        intent.putExtra(KeyConstant.B_PIC_PATH, "");
//        intent.putExtra(KeyConstant.B_PIC_NAME, "");
        this.setResult(RESULT_OK, intent);
        finish();
    }

    private void recycleBitmap(Bitmap bitmap) {
        // 先判断是否已经回收
        if (bitmap != null && !bitmap.isRecycled()) {
            // 回收并且置为null
            bitmap.recycle();
            bitmap = null;
        }
        System.gc();
    }
}
