package ru.oepak22.simplemusicplayer.screen.tracks

import ru.oepak22.simplemusicplayer.content.AudioTrack
import java.util.ArrayList

// интерфейс для презентера списка треков
interface MusicListView {
    fun showTracks(list: ArrayList<AudioTrack>, sortOrder: Int)             // вывод содержимого на экран
    fun showEmptyTracks()                                                   // вывод пустого пространства на экран
}