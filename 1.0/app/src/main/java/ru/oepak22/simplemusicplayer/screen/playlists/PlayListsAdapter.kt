package ru.oepak22.simplemusicplayer.screen.playlists

import android.view.LayoutInflater
import android.view.ViewGroup
import ru.oepak22.simplemusicplayer.R
import ru.oepak22.simplemusicplayer.content.PlayList
import ru.oepak22.simplemusicplayer.widget.BaseAdapter

// класс адаптера для списка плейлистов
class PlayListsAdapter(items: ArrayList<PlayList>, dialog: Boolean):
    BaseAdapter<PlayListsHolder, PlayList>(items) {

    // флаг - диалог или обычная активность
    private val isDialog = dialog

    // инициализация отображателя элементов списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListsHolder {
        return PlayListsHolder(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_playlist,
                parent,
                false
            ), isDialog)
    }

    // оформление каждого элемента списка
    override fun onBindViewHolder(holder: PlayListsHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val audioTrack = getItem(position)
        holder.bind(audioTrack)
    }
}