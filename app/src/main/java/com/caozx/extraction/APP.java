package com.caozx.extraction;

import android.app.Application;

/**
 * Created by Czx on 2016/8/2.
 */
public class APP extends Application{
    private static APP theInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        theInstance = this;

//        Recovery.getInstance()
//                .debug(true)
//                .recoverInBackground(false)
//                .recoverStack(true)
//                .mainPage(MainActivity.class)
//                .callback(new MyCrashCallback())
//                .init(this);
    }


    public static APP getInstance() {
        return theInstance;
    }
}
