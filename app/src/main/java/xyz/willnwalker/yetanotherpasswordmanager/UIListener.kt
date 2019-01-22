package xyz.willnwalker.yetanotherpasswordmanager

import io.realm.RealmConfiguration

interface UIListener {
    fun setRealmConfig(realmConfig: RealmConfiguration)
    fun getRealmConfig(): RealmConfiguration
    fun exit()
}