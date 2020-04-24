package xyz.willnwalker.yetanotherpasswordmanager

import android.app.Application
import com.facebook.stetho.Stetho
import io.realm.Realm

@Suppress("unused")
class YetAnotherPasswordManager : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        if(BuildConfig.DEBUG){
            Stetho.initializeWithDefaults(this)
        }
    }

}