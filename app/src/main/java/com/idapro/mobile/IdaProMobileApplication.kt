package com.idapro.mobile

import android.app.Application
import androidx.room.Room
import com.idapro.mobile.data.database.AppDatabase

class IdaProMobileApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "ida_pro_mobile.db"
        ).build()
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Load native library
        System.loadLibrary("binary_analyzer")
    }
}
