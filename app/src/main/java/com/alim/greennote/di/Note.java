package com.alim.greennote.di;

import android.app.Application;

import com.nelu.ncbase.BaseNC;

public class Note extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Injection.INSTANCE.init(this);
        BaseNC.INSTANCE.init(
                this, "98a1c01e-e4a4-49a1-bb08-21352a4602ee", null
        );
    }
}
