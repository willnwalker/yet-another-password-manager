package xyz.willnwalker.yetanotherpasswordmanager

import androidx.lifecycle.ViewModel
import io.realm.RealmConfiguration

class SharedViewModel: ViewModel(){

    var realmConfig: RealmConfiguration? = null

}