package xyz.willnwalker.yetanotherpasswordmanager

import android.app.Application
import io.realm.Realm

class YetAnotherPasswordManager : Application(){

    override fun onCreate(){
        super.onCreate()
        // Initialize Realm (just once per application)
        Realm.init(applicationContext)
    }

}