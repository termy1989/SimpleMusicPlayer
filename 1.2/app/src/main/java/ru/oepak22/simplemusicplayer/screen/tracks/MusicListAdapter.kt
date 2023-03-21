package ru.oepak22.simplemusicplayer.screen.tracks

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.oepak22.simplemusicplayer.R
import ru.oepak22.simplemusicplayer.content.AudioTrack
import ru.oepak22.simplemusicplayer.content.Defines
import ru.oepak22.simplemusicplayer.widget.BaseAdapter

// класс адаптера для списка треков
class MusicListAdapter(items: ArrayList<AudioTrack>) : BaseAdapter<MusicListHolder,
        AudioTrack>(items) {

    // инициализация отображателя элементов списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicListHolder {
        return MusicListHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_track, parent, false))
    }

    // оформление каждого элемента списка
    override fun onBindViewHolder(holder: MusicListHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val audioTrack = getItem(position)
        holder.bind(audioTrack)
    }

    // сортировка списка
    fun sortByField(sort: Int) {

        if (mItems.size >= 2) {
            mItems.sortWith { p0, p1 ->
                var res = 0
                when (sort) {
                    Defines.SORT_BY_TITLE -> {
                        val pp0 = p0 as AudioTrack
                        val pp1 = p1 as AudioTrack
                        res = pp0.mTitle.compareTo(pp1.mTitle, ignoreCase = true)
                    }
                    Defines.SORT_BY_ARTIST -> {
                        val pp0 = p0 as AudioTrack
                        val pp1 = p1 as AudioTrack
                        res = pp0.mArtist.compareTo(pp1.mArtist, ignoreCase = true)
                    }
                    Defines.SORT_BY_ALBUM -> {
                        val pp0 = p0 as AudioTrack
                        val pp1 = p1 as AudioTrack
                        res = pp0.mAlbum.compareTo(pp1.mAlbum, ignoreCase = true)
                    }
                }
                res
            }
            notifyDataSetChanged()
        }
    }
}