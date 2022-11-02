package ru.oepak22.simplemusicplayer.screen.playlists

import android.graphics.Color
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.oepak22.simplemusicplayer.R
import ru.oepak22.simplemusicplayer.content.PlayList

// класс отображателя элемента списка
class PlayListsHolder(itemView: View, dialog: Boolean) : RecyclerView.ViewHolder(itemView) {

    // составляющие элемента списка
    private val playlistTextView: TextView = itemView.findViewById(R.id.playlist_name_text_view)
    private val imageView: ImageView = itemView.findViewById(R.id.playlist_icon_view)
    private val checkBox: CheckBox = itemView.findViewById(R.id.playlist_check_box)
    private var isDialog = dialog

    // оформление каждого элемента списка
    fun bind(playList: PlayList) {

        playlistTextView.text = playList.mName

        // в диалоговом окне элемент нельзя пометить как выбранный
        if (isDialog) {
            checkBox.visibility = View.GONE
            val params: LinearLayout.LayoutParams = imageView.layoutParams as LinearLayout.LayoutParams
            params.marginStart = 15
            imageView.layoutParams = params
        }

        // checkbox для выбора элемента
        else {
            checkBox.visibility = View.VISIBLE
            checkBox.isChecked = playList.mSelected
            checkBox.setOnClickListener {
                playList.mSelected = !playList.mSelected
            }
        }
    }
}