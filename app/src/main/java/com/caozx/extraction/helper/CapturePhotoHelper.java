package com.caozx.extraction.helper;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.caozx.extraction.R;
import com.caozx.extraction.utils.BitmapUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 拍照辅助类
 * Created by Clock on 2016/5/21.
 */
public class CapturePhotoHelper {

    private Activity mActivity;

    private File mPhotoFolder;//存放图片的目录
    private File mPhotoFile;//拍照生成的图片文件

    /**
     * @param activity
     * @param photoFolder 存放生成照片的目录，目录不存在时候会自动创建，但不允许为null;
     */
    public CapturePhotoHelper(Activity activity, File photoFolder) {
        this.mActivity = activity;
        this.mPhotoFolder = photoFolder;
    }

    /**
     * 拍照
     */
    public void capture(int requestCode, String picName) {
        if (hasCamera()) {
            createPhotoFile(picName);

            if (mPhotoFile == null) {
                Toast.makeText(mActivity, R.string.camera_open_error, Toast.LENGTH_SHORT).show();
                return;
            }

            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri fileUri = Uri.fromFile(mPhotoFile);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            mActivity.startActivityForResult(captureIntent, requestCode);

        } else {
            Toast.makeText(mActivity, R.string.camera_open_error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 创建照片文件
     */
    private void createPhotoFile(String picName) {
        if (mPhotoFolder != null) {
            if (!mPhotoFolder.exists()) {//检查保存图片的目录存不存在
                mPhotoFolder.mkdirs();
            }

            mPhotoFile = new File(mPhotoFolder, picName + BitmapUtils.JPG_SUFFIX);
            if (mPhotoFile.exists()) {
                mPhotoFile.delete();
            }
            try {
                mPhotoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                mPhotoFile = null;
            }
        } else {
            mPhotoFile = null;
            Toast.makeText(mActivity, R.string.not_specify_a_directory, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 判断系统中是否存在可以启动的相机应用
     * @return 存在返回true，不存在返回false
     */
    public boolean hasCamera() {
        PackageManager packageManager = mActivity.getPackageManager();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * 获取当前拍到的图片文件
     */
    public File getPhoto() {
        return mPhotoFile;
    }

    /**
     * 设置照片文件
     */
    public void setPhoto(File photoFile) {
        this.mPhotoFile = photoFile;
    }
}
