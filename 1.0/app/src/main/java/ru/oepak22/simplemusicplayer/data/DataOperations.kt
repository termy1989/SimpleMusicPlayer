package ru.oepak22.simplemusicplayer.data

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import io.realm.Realm
import ru.oepak22.simplemusicplayer.content.AudioTrack
import ru.oepak22.simplemusicplayer.content.EqualizerSettings
import ru.oepak22.simplemusicplayer.content.PlayList
import ru.oepak22.simplemusicplayer.content.PlaylistsComparison
import javax.inject.Inject

// класс операций с сохраняемыми данными
class DataOperations @Inject constructor() : DataService {

    // сохранение списка треков в базе Realm
    override fun saveTrackList(list: ArrayList<AudioTrack>?) {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        if (list != null) {
            realm.delete(AudioTrack::class.java)
            realm.insert(list)
        }
        realm.commitTransaction()
        realm.close()
    }

    // восстановление списка треков из базы Realm
    override fun restoreTrackList(): ArrayList<AudioTrack>? {
        val realm = Realm.getDefaultInstance()
        val results = realm.where(AudioTrack::class.java).findAll()
        val list = realm.copyFromRealm(results) as ArrayList<AudioTrack>
        realm.close()
        return if (list.isEmpty()) null
        else list
    }

    // сохранение текущего плейлиста в Shared Preferences
    @SuppressLint("CommitPrefEdits")
    override fun saveCurrentPlaylist(context: Context, currentPlaylist: String) {
        val settings = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString("Current_List", currentPlaylist)
        editor.apply()
    }

    // восстановление текущего плейлиста из Shared Preferences
    override fun restoreCurrentPlaylist(context: Context): String {
        val settings = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return if (settings.contains("Current_List"))
            settings.getString("Current_List", "").toString()
        else ""
    }

    // сохранение настроек эквалайзера в Json
    override fun saveEqualizerSettings(context: Context, equalizerSettings: EqualizerSettings) {
        val settings = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString("Equalizer_Settings", Gson().toJson(equalizerSettings))
        editor.apply()
    }

    // восстановление настроек эквалайзера из Json
    override fun restoreEqualizerSettings(context: Context): EqualizerSettings? {
        val settings = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return if (settings.contains("Equalizer_Settings"))
            Gson().fromJson(settings.getString("Equalizer_Settings", "{}"), EqualizerSettings::class.java)
        else null
    }

    // добавление нового плейлиста
    override fun addNewPlaylist(newPlaylist: String): Boolean {

        return if (findPlayList(newPlaylist) != null || newPlaylist == "") false
        else {
            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            realm.insert(PlayList(newPlaylist))
            realm.commitTransaction()
            realm.close()
            true
        }
    }

    // редактирование выбранного плейлиста
    override fun editSelectedPlaylist(oldPlaylistName: String, newPlaylistName: String): Boolean {
        return if (newPlaylistName == "" || findPlayList(newPlaylistName) != null)
            false
        else {
            val realm = Realm.getDefaultInstance()

            // поиск плейлиста с указанным старым именем
            val playList: PlayList? = realm.where(PlayList::class.java)
                .equalTo("mName", oldPlaylistName)
                .findFirst()

            // поиск сопоставлений треков с указанным плейлистом
            val comparison = realm.where(PlaylistsComparison::class.java)
                .equalTo("mPlaylistName", oldPlaylistName)
                .findAll()

            // изменение имени указанного плейлиста и сопоставлений
            realm.beginTransaction()
            var status = false
            if (playList != null) {
                playList.mName = newPlaylistName
                for (item in comparison) item.mPlaylistName = newPlaylistName
                status = true
            }
            realm.commitTransaction()
            realm.close()
            status
        }
    }

    // удаление выбранного плейлиста
    override fun delSelectedPlaylist(selectedPlaylist: ArrayList<PlayList>): Boolean {

        val realm = Realm.getDefaultInstance()
        var status = false

        for (playlist in selectedPlaylist) {

            // поиск плейлиста в базе
            realm.beginTransaction()

            // поиск сопоставлений треков с указанным плейлистом
            val comparison = realm.where(PlaylistsComparison::class.java)
                .equalTo("mPlaylistName", playlist.mName)
                .findAll()

            // поиск указанного плейлиста
            val result = realm.where(PlayList::class.java)
                .equalTo("mName", playlist.mName)
                .findFirst()

            // удаление плейлиста и сопоставлений из базы
            status = if (result != null) {
                result.deleteFromRealm()
                for (item in comparison) item.deleteFromRealm()
                true
            } else false

            realm.commitTransaction()
        }

        realm.close()
        return status
    }

    // вывод списка плейлистов
    override fun showAllPlaylists(): ArrayList<PlayList>? {
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        val results = realm.where(PlayList::class.java).findAll()
        val list = realm.copyFromRealm(results) as ArrayList<PlayList>
        realm.commitTransaction()
        realm.close()
        return if (list.isEmpty()) null
        else list
    }

    // поиск плейлиста по заданному имени
    override fun findPlayList(playlistName: String): PlayList? {

        var result: PlayList? = null
        val realm = Realm.getDefaultInstance()

        // поиск элемента в базе данных
        val playlists = realm.where(PlayList::class.java)
            .equalTo("mName", playlistName)
            .findAll()

        // элемент найден - заполнение полей значениями элемента
        val list = realm.copyFromRealm(playlists) as ArrayList<PlayList>
        if (list.size == 1) result = list[0]

        realm.close()
        return result
    }

    // добавление треков в указанный плейлист
    override fun addTracksToPlaylist(playlistName: String, trackList: ArrayList<AudioTrack>) {

        val realm = Realm.getDefaultInstance()

        // цикл по всем указанным трекам
        for (item in trackList) {

            // поиск имеющихся сопоставлений
            val comparison = realm.where(PlaylistsComparison::class.java)
                .equalTo("mTrackPath", item.mPath)
                .and()
                .equalTo("mPlaylistName", playlistName)
                .findFirst()

            // создание сопоставления для очередного трека из списка
            if (comparison == null) {
                realm.beginTransaction()
                realm.insert(PlaylistsComparison(item.mPath, playlistName))
                realm.commitTransaction()
            }
        }

        realm.close()
    }

    // удаление треков из указанного плейлиста
    override fun delTracksFromPlaylist(playlistName: String, trackList: ArrayList<AudioTrack>) {

        val realm = Realm.getDefaultInstance()

        // цикл по всем указанным трекам
        for (item in trackList) {

            // поиск имеющихся сопоставлений
            val comparison = realm.where(PlaylistsComparison::class.java)
                .equalTo("mTrackPath", item.mPath)
                .and()
                .equalTo("mPlaylistName", playlistName)
                .findFirst()

            // удаление сопоставления для очередного трека из списка
            if (comparison != null) {
                realm.beginTransaction()
                comparison.deleteFromRealm()
                realm.commitTransaction()
            }
        }

        realm.close()
    }

    // поиск треков в указанном плейлисте
    override fun findTracksInPlaylist(playlistName: String): ArrayList<String>? {

        val realm = Realm.getDefaultInstance()
        val results = realm.where(PlaylistsComparison::class.java)
            .equalTo("mPlaylistName", playlistName)
            .findAll()
        val trackList = realm.copyFromRealm(results) as ArrayList<PlaylistsComparison>
        realm.close()
        return if (trackList.isEmpty()) null
        else {
            val strList = ArrayList<String>()
            for (item in trackList) strList.add(item.mTrackPath)
            strList
        }
    }
}