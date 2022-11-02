package ru.oepak22.simplemusicplayer.content

import io.realm.RealmObject

// класс для сопоставления названия плейлиста и пути к треку
open class PlaylistsComparison() : RealmObject() {

    var mTrackPath = ""                                             // путь к треку
    var mPlaylistName = ""                                          // имя плейлиста

    constructor(initTrackPath: String,
                initPlaylistName: String) : this() {
        mTrackPath = initTrackPath
        mPlaylistName = initPlaylistName
    }
}