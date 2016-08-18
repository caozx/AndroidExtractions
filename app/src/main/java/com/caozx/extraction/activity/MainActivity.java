package com.caozx.extraction.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.caozx.extraction.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int ACTIVITY_REQUEST_CODE = 0x110;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_camera_take_photo).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case ACTIVITY_REQUEST_CODE:
                PhotographActivity.start(this, ACTIVITY_REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ACTIVITY_REQUEST_CODE:
//                    String picPath = data.getExtras().getString(KeyConstant.B_PIC_PATH);
//                    String picName = data.getExtras().getString(KeyConstant.B_PIC_NAME);
                    break;
            }
        }
    }
}
