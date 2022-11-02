package ru.oepak22.simplemusicplayer.screen.tracks

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.oepak22.simplemusicplayer.R
import ru.oepak22.simplemusicplayer.widget.BaseAdapter
import ru.oepak22.simplemusicplayer.content.AudioTrack

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
}