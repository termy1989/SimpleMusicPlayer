package ru.oepak22.simplemusicplayer.screen.tracks

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.oepak22.simplemusicplayer.R
import ru.oepak22.simplemusicplayer.content.AudioTrack
import ru.oepak22.simplemusicplayer.content.Defines

// класс отображателя элемента списка
class MusicListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    // составляющие элемента списка
    private val titleTextView : TextView = itemView.findViewById(R.id.music_title_text)
    private val artistTextView : TextView = itemView.findViewById(R.id.music_artist_text)
    private val imageView : ImageView = itemView.findViewById(R.id.icon_view)

    // оформление каждого элемента списка
    @SuppressLint("ResourceAsColor")
    fun bind(audioTrack: AudioTrack) {

        titleTextView.text = audioTrack.mTitle
        artistTextView.text = audioTrack.mArtist

        if (audioTrack.mSelected)
            itemView.setBackgroundColor(Color.parseColor("#C0C7CD"))
        else
            itemView.setBackgroundColor(Color.parseColor("#FFFFFF"))

        /*when (audioTrack.mStatus) {
            Defines.TRACK_IS_STOPPED -> {
                imageView.setImageResource(R.drawable.nav_icon)
                titleTextView.setTextColor(Color.parseColor("#000000"))
            }
            Defines.TRACK_IS_PLAYED -> {
                imageView.setImageResource(R.drawable.ic_baseline_play_24)
                titleTextView.setTextColor(Color.parseColor("#FF0000"))
            }
            Defines.TRACK_IS_PAUSED -> {
                imageView.setImageResource(R.drawable.ic_baseline_pause_24)
                titleTextView.setTextColor(Color.parseColor("#FF0000"))
            }
        }*/

        imageView.setImageResource(R.drawable.nav_icon)
        if (audioTrack.mStatus == Defines.TRACK_IS_STOPPED)
            titleTextView.setTextColor(Color.parseColor("#000000"))
        else
            titleTextView.setTextColor(Color.parseColor("#FF0000"))

    }
}