package ru.oepak22.simplemusicplayer.screen.tracks

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import ru.oepak22.simplemusicplayer.content.AudioTrack
import ru.oepak22.simplemusicplayer.content.Defines
import ru.oepak22.simplemusicplayer.data.DataService
import java.io.File

// презентер списка треков
class MusicListPresenter(initView: MusicListView, initService: DataService) {

    private var currentAudioTrackTitle = ""
    private var currentAudioTrackStatus = Defines.TRACK_IS_STOPPED
    private var mView = initView
    private var mService = initService
    private var mCurrentList = ""
    private var mCachedList: ArrayList<AudioTrack>? = null

    // обновление списка треков
    @SuppressLint("Recycle")
    fun refresh(title: String?, status: Int) {

        currentAudioTrackTitle = title!!
        currentAudioTrackStatus = status
        mCurrentList = mService.restoreCurrentPlaylist(mView as Context)

        // проверка наличия закешированного списка треков
        if (mCachedList != null) {
            Log.v("MusicListPresenter", "Restore from cache")
            mView.showTracks(mCachedList!!)
            mCachedList = null
        }

        // поиск и вывод треков
        else {
            Log.v("MusicListPresenter", "Refresh")

            // инициализация
            val songsList: ArrayList<AudioTrack> = ArrayList()

            // указание требуемых свойств трека
            val projection = arrayOf(
                MediaStore.Audio.Media.TITLE,                           // mTitle
                MediaStore.Audio.Media.ARTIST,                          // mArtist
                MediaStore.Audio.Media.ALBUM,                           // mAlbum
                MediaStore.Audio.Media.DISPLAY_NAME,                    // mFileName
                MediaStore.Audio.Media.DATA,                            // mPath
                //MediaStore.Audio.Media.DURATION                         // mDuration
            )

            // проверка, что найденный файл является музыкальным
            val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

            // поиск всех аудио-треков, имеющихся в памяти смартфона
            val cursor = (mView as Context).contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
            )

            if (cursor != null) {

                // заполнение списка треков
                while (cursor.moveToNext()) {

                    // формирование элемента списка
                    val title = cursor.getString(0)
                    val artist = cursor.getString(1)
                    val album = cursor.getString(2)
                    val name = cursor.getString(3)
                    val path = cursor.getString(4)
                    val songData = AudioTrack(title, artist, album, name, path, 0, 0)
                    if (title == currentAudioTrackTitle)
                        songData.mStatus = currentAudioTrackStatus
                    else songData.mStatus = Defines.TRACK_IS_STOPPED

                    // проверка трека на существование
                    val file = File(songData.mPath)
                    if (file.exists()) {

                        // список треков указанного плейлиста
                        if (mCurrentList != "") {
                            val list = mService.findTracksInPlaylist(mCurrentList)
                            if (list != null && list.contains(songData.mPath))
                                songsList.add(songData)
                        }

                        // список всех треков
                        else songsList.add(songData)
                    }
                }

                // вывод списка треков или пустого пространства
                if (songsList.size == 0) mView.showEmptyTracks()
                else mView.showTracks(songsList)
                cursor.close()
            } else mView.showEmptyTracks()
        }
    }

    // добавление треков в плейлист
    fun addTracksToPlaylist(playlistName: String, trackList: ArrayList<AudioTrack>) {
        val selectedTracks = ArrayList<AudioTrack>()
        for (item in trackList) {
            if (item.mSelected)
                selectedTracks.add(item)
        }
        mService.addTracksToPlaylist(playlistName, selectedTracks)
        clearSelection(trackList)
    }

    // удаление треков из плейлиста
    fun delTracksFromPlaylist(trackList: ArrayList<AudioTrack>) {
        val selectedTracks = ArrayList<AudioTrack>()
        for (item in trackList) {
            if (item.mSelected)
                selectedTracks.add(item)
        }
        mService.delTracksFromPlaylist(mCurrentList, selectedTracks)
        refresh(currentAudioTrackTitle, currentAudioTrackStatus)
    }

    // сохранение списка треков перед поворотом экрана
    fun cacheTrackList(list: ArrayList<AudioTrack>) {
        mService.saveTrackList(list)
    }

    // очистка списка треков
    fun restoreTrackList() {
        mCachedList = mService.restoreTrackList()
    }

    // переключение плейлиста
    fun selectPlaylist(name: String) {
        mCurrentList = name
        mService.saveCurrentPlaylist((mView as Context), name)
        refresh(currentAudioTrackTitle, currentAudioTrackStatus)
    }

    // проверка, есть ли в указанном списке выбранные элементы
    fun isSelectedExist(list: List<AudioTrack>) : Boolean {
        for (track in list) {
            if (track.mSelected)
                return true
        }
        return false
    }

    // выбор элемента из списка треков
    fun selectItem(item: AudioTrack, list: ArrayList<AudioTrack>) {
        val newArray: ArrayList<AudioTrack> = ArrayList()
        for (track in list) {
            if (track == item)
                track.mSelected = !item.mSelected
            newArray.add(track)
        }
        mView.showTracks(newArray)
    }

    // снятие всех выделений в списке треков
    fun clearSelection(list: ArrayList<AudioTrack>) {
        val newArray: ArrayList<AudioTrack> = ArrayList()
        for (track in list) {
            track.mSelected = false
            newArray.add(track)
        }
        mView.showTracks(newArray)
    }

    // проверка выбранного трека на существование
    fun isTrackExist(track: AudioTrack) : Boolean {
        val file = File(track.mPath)
        return file.exists()
    }
}

/*            Collections.sort(remoteFiles, (file1, file2) -> {
                boolean b1 = file1.isDirectory();
                boolean b2 = file2.isDirectory();
                return (b1 != b2) ? (b1) ? -1 : 1 : 0;
            });*/