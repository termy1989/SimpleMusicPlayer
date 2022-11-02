package ru.oepak22.simplemusicplayer.content

import io.realm.RealmObject

// класс параметров плейлиста
open class PlayList() : RealmObject() {

    var mName = ""                              // название плейлиста
    var mSelected = false                       // плейлист выбран

    constructor(initName: String) : this() {
        mName = initName
    }
}