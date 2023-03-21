package ru.oepak22.simplemusicplayer.content

import io.realm.RealmObject
import java.io.Serializable
import java.text.FieldPosition

// класс параметров музыкального трека
open class AudioTrack() : RealmObject(), Serializable {

    var mTitle = ""                                         // отображаемое имя трека
    var mArtist = ""                                        // исполнитель
    var mAlbum = ""                                         // музыкальный альбом

    var mFileName = ""                                      // имя файла трека
    var mPath = ""                                          // полный путь к файлу трека

    var mPosition = 0                                       // текущее положение трека
    var mDuration = 0                                       // продолжительность трека

    var mSelected = false                                   // трек выбран
    var mStatus = Defines.TRACK_IS_STOPPED                  // трек проигрывается или на паузе

    constructor(initTitle: String,
                initArtist: String,
                initAlbum: String,
                initFileName: String,
                initPath: String,
                initPosition: Int,
                initDuration: Int) : this() {
        mTitle = initTitle
        mArtist = initArtist
        mAlbum = initAlbum
        mFileName = initFileName
        mPath = initPath
        mPosition = initPosition
        mDuration = initDuration
    }
}