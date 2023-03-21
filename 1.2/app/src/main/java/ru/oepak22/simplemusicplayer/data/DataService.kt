package ru.oepak22.simplemusicplayer.data

import android.content.Context
import ru.oepak22.simplemusicplayer.content.AudioTrack
import ru.oepak22.simplemusicplayer.content.EqualizerSettings
import ru.oepak22.simplemusicplayer.content.PlayList

// интерфейс для операций с данными
interface DataService {

    fun saveTrackList(list: ArrayList<AudioTrack>?)
    fun restoreTrackList(): ArrayList<AudioTrack>?

    fun saveCurrentPlaylist(context: Context, currentPlaylist: String)
    fun restoreCurrentPlaylist(context: Context): String

    fun saveEqualizerSettings(context: Context, equalizerSettings: EqualizerSettings)
    fun restoreEqualizerSettings(context: Context): EqualizerSettings?

    fun saveSortOrder(context: Context, order: Int)
    fun restoreSortOrder(context: Context): Int

    fun addNewPlaylist(newPlaylist: String): Boolean
    fun editSelectedPlaylist(oldPlaylistName: String, newPlaylistName: String): Boolean
    fun delSelectedPlaylist(selectedPlaylist: ArrayList<PlayList>): Boolean
    fun showAllPlaylists(): ArrayList<PlayList>?
    fun findPlayList(playlistName: String): PlayList?

    fun addTracksToPlaylist(playlistName: String, trackList: ArrayList<AudioTrack>)
    fun delTracksFromPlaylist(playlistName: String, trackList: ArrayList<AudioTrack>)
    fun findTracksInPlaylist(playlistName: String): ArrayList<String>?
}