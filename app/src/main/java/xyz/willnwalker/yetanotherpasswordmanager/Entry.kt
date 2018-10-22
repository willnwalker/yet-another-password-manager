package xyz.willnwalker.yetanotherpasswordmanager

import io.realm.RealmObject

/* This is the Realm xyz.willnwalker.yetanotherpasswordmanager.Entry Objects, basically the data model schema */
class Entry(var id: String?) : RealmObject() {
    var title: String? = null
    var userName: String? = null
    var password: String? = null
    var url: String? = null
    var notes: String? = null

    init {
        userName = ""
        password = ""
        url = ""
        notes = ""
    }
}