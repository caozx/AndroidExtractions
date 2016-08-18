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
    }


    public static APP getInstance() {
        return theInstance;
    }
}
