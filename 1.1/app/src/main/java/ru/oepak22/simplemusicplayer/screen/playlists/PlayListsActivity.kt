package ru.oepak22.simplemusicplayer.screen.playlists

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.oepak22.simplemusicplayer.MainApp
import ru.oepak22.simplemusicplayer.R
import ru.oepak22.simplemusicplayer.content.PlayList
import ru.oepak22.simplemusicplayer.data.DataOperations
import ru.oepak22.simplemusicplayer.data.DataService
import ru.oepak22.simplemusicplayer.widget.BaseAdapter
import ru.oepak22.simplemusicplayer.widget.EmptyRecyclerView
import javax.inject.Inject

// класс активности для работы с плейлистами
class PlayListsActivity : AppCompatActivity(),
                            PlayListsView,
                            BaseAdapter.OnItemClickListener<PlayList>{

    private lateinit var mPlayListsPresenter: PlayListsPresenter                                    // презентер для работы с плейлистами
    private lateinit var mAdapter: PlayListsAdapter                                                 // адаптер списка

    private lateinit var mButtonAdd: FloatingActionButton                                           // кнопка добавления плейлиста
    private lateinit var mButtonDel: FloatingActionButton                                           // кнопка удаления плейлиста

    private var mDialog: AlertDialog? = null                                                        // диалоговое сообщение
    @Inject lateinit var mService: DataOperations

    // создание активности
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlists)

        //
        MainApp.sAppComponent.injectPlayListsActivity(this)

        // инициализация презентера
        mPlayListsPresenter = PlayListsPresenter(this, mService)

        // компоненты активности
        val mRecyclerView: EmptyRecyclerView = findViewById(R.id.playlists_recycler_view)
        val mEmptyView: View = findViewById(R.id.noplaylists_textview)

        // настройка списка
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.mEmptyView = mEmptyView

        // инициализация адаптера списка
        mAdapter = PlayListsAdapter(ArrayList(), false)
        mAdapter.attachToRecyclerView(mRecyclerView)

        // инициализация кнопок
        mButtonAdd = findViewById(R.id.fab_add)
        mButtonDel = findViewById(R.id.fab_del)
    }

    // возобновление работы активности
    override fun onResume() {
        super.onResume()

        // установка обработчика нажатия на элемент списка
        mAdapter.mOnItemClickListener = this

        // установка обработчиков нажатия на кнопки
        mButtonAdd.setOnClickListener { addPlaylistDialog() }
        mButtonDel.setOnClickListener { delPlaylistDialog() }

        // вывод списка плейлистов
        mPlayListsPresenter.showPlaylists()
    }

    // приостановка работы активности
    override fun onPause() {
        super.onPause()
        mAdapter.mOnItemClickListener = null
        mButtonAdd.setOnClickListener(null)
        mButtonDel.setOnClickListener(null)
    }

    // уничтожение активности
    override fun onDestroy() {
        super.onDestroy()
        mDialog?.dismiss()
    }

    // вывод списка плейлистов
    override fun showPlaylists(list: ArrayList<PlayList>) {
        mAdapter.changeDataSet(list)
    }

    // вывод пустого пространства
    override fun showEmptyPlaylists() {
        mAdapter.clear()
    }

    // сообщение об успешном завершении операции
    override fun showSuccess(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        mPlayListsPresenter.showPlaylists()
    }

    // сообщение об ошибке при выполнении операции
    override fun showError(msg: String) {
        val builder = AlertDialog.Builder(this)
        with (builder) {
            setIcon(R.drawable.ic_message_error)
            setTitle(R.string.message_error)
            setMessage(msg)
            setPositiveButton(R.string.message_ok) { _, _ -> }
        }
        mDialog = builder.create()
        mDialog!!.show()
    }

    // нажатие на элемент списка - редактирование
    override fun onItemClick(item: PlayList) {
        editPlayDialog(item.mName)
    }

    // добавление нового плейлиста
    @SuppressLint("InflateParams")
    private fun addPlaylistDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.edit_text_layout, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editText)

        with (builder) {
            setTitle(R.string.message_add_title)
            setPositiveButton(R.string.message_ok) { _, _ ->
                mPlayListsPresenter.addNewPlaylist(editText.text.toString())
            }
            setNegativeButton(R.string.message_cancel) { _, _ -> }
            setView(dialogLayout)
        }
        mDialog = builder.create()
        mDialog!!.show()
    }

    //
    @SuppressLint("InflateParams")
    private fun editPlayDialog(oldName: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.edit_text_layout, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editText)
        editText.setText(oldName)
        editText.hint = "New name"

        with (builder) {
            setTitle(R.string.message_edit_title)
            setPositiveButton(R.string.message_ok) { _, _ ->
                mPlayListsPresenter.editSelectedPlaylist(oldName, editText.text.toString())
            }
            setNegativeButton(R.string.message_cancel) { _, _ -> }
            setView(dialogLayout)
        }
        mDialog = builder.create()
        mDialog!!.show()
    }

    // удаление выбранных плейлистов
    private fun delPlaylistDialog() {
        val list = ArrayList<PlayList>()
        for (item in mAdapter.mItems) {
            if (item.mSelected)
                list.add(item)
        }
        if (list.size == 0) showError("Please, select at least one item for deleting")
        else {
            val builder = AlertDialog.Builder(this)
            with (builder) {
                setIcon(R.drawable.ic_message_warning)
                setTitle(R.string.message_warning)
                setMessage(R.string.message_sure_delete)
                setPositiveButton(R.string.message_ok) { _, _ ->
                    mPlayListsPresenter.deleteSelectedPlaylist(list)
                }
                setNegativeButton(R.string.message_cancel) { _, _ -> }
            }
            mDialog = builder.create()
            mDialog!!.show()
        }
    }
}