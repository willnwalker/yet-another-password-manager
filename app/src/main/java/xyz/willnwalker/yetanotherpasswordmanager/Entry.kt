package xyz.willnwalker.yetanotherpasswordmanager

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/* This is the Realm xyz.willnwalker.yetanotherpasswordmanager.Entry Objects, basically the data model schema */
@RealmClass
open class Entry : RealmModel {
    @PrimaryKey
    var id = UUID.randomUUID().toString()
    var title = ""
    var userName = ""
    var password = ""
    var url = ""
    var notes = ""
}