package xyz.willnwalker.yetanotherpasswordmanager

import android.app.Application
import com.facebook.stetho.Stetho

@Suppress("unused")
class YetAnotherPasswordManager : Application() {

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG){
            Stetho.initializeWithDefaults(this)
        }
    }

}