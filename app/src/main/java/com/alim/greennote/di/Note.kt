package com.alim.greennote.di

import android.app.Application
import com.nelu.ncbase.BaseNC.init

class Note : Application() {

    override fun onCreate() {
        super.onCreate()
        Injection.init(this)
        init(
            this, "98a1c01e-e4a4-49a1-bb08-21352a4602ee", null
        )
    }
}
