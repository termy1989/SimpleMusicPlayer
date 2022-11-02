package ru.oepak22.simplemusicplayer.screen.playlists

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import ru.oepak22.simplemusicplayer.MainApp
import ru.oepak22.simplemusicplayer.R
import ru.oepak22.simplemusicplayer.content.PlayList
import ru.oepak22.simplemusicplayer.data.DataOperations
import ru.oepak22.simplemusicplayer.data.DataService
import ru.oepak22.simplemusicplayer.screen.tracks.MusicListActivity
import ru.oepak22.simplemusicplayer.widget.BaseAdapter
import ru.oepak22.simplemusicplayer.widget.EmptyRecyclerView
import javax.inject.Inject

// класс диалогового окна для выбора плейлиста
class PlayListsDialog : DialogFragment(),
                        PlayListsView,
                        BaseAdapter.OnItemClickListener<PlayList>{

    private lateinit var mPlayListsPresenter: PlayListsPresenter                                    // презентер для работы с плейлистами
    private lateinit var mAdapter: PlayListsAdapter                                                 // адаптер списка
    @Inject lateinit var mService: DataOperations

    // создание диалога
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //
        MainApp.sAppComponent.injectPlayListsDialog(this)

        // инициализация презентера
        mPlayListsPresenter = PlayListsPresenter(this, mService)
    }

    // отрисовка диалога
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = requireActivity()
            .layoutInflater
            .inflate(R.layout.activity_playlists, null)

        // сокрытие тулбара
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_playlists)
        toolbar.visibility = View.GONE

        // сокрытие кнопок
        view.findViewById<View>(R.id.fab_add).visibility = View.GONE
        view.findViewById<View>(R.id.fab_del).visibility = View.GONE

        // компоненты активности
        val mRecyclerView: EmptyRecyclerView = view.findViewById(R.id.playlists_recycler_view)
        val mEmptyView: View = view.findViewById(R.id.noplaylists_textview)

        // настройка списка
        mRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        mRecyclerView.mEmptyView = mEmptyView

        // инициализация адаптера списка
        mAdapter = PlayListsAdapter(ArrayList(), true)
        mAdapter.attachToRecyclerView(mRecyclerView)

        // формирование диалога
        val builder = AlertDialog.Builder(requireActivity())
        with (builder) {
            setTitle(R.string.message_playlist)
            setNegativeButton(R.string.message_cancel) { _, _ -> dismiss() }
            setView(view)
        }

        onViewCreated(view, null)
        return builder.create()
    }

    // возобновление работы диалога
    override fun onResume() {
        super.onResume()
        mAdapter.mOnItemClickListener = this
        mPlayListsPresenter.showPlaylists()
    }

    // приостановка работы диалога
    override fun onPause() {
        super.onPause()
        mAdapter.mOnItemClickListener = null
    }

    // вывод списка плейлистов
    override fun showPlaylists(list: ArrayList<PlayList>) {
        mAdapter.changeDataSet(list)
    }

    // вывод пустого пространства
    override fun showEmptyPlaylists() {
        mAdapter.clear()
    }

    // выбор плейлиста
    override fun onItemClick(item: PlayList) {
        (requireActivity() as MusicListActivity).addTrackInPlaylist(item.mName)
        dismiss()
    }

    override fun showSuccess(msg: String) {}

    override fun showError(msg: String) {}
}