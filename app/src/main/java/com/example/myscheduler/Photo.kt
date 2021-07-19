package com.example.myscheduler

import android.net.Uri
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class Photo: RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var uri: String = ""
}