package ru.oepak22.simplemusicplayer.screen.tracks

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import ru.oepak22.simplemusicplayer.MainApp
import ru.oepak22.simplemusicplayer.R
import ru.oepak22.simplemusicplayer.content.AudioTrack
import ru.oepak22.simplemusicplayer.content.Defines
import ru.oepak22.simplemusicplayer.content.PlayList
import ru.oepak22.simplemusicplayer.data.DataOperations
import ru.oepak22.simplemusicplayer.media.MusicPlayerActivity
import ru.oepak22.simplemusicplayer.media.MusicPlayerService
import ru.oepak22.simplemusicplayer.screen.equalizer.EqualizerActivity
import ru.oepak22.simplemusicplayer.screen.playlists.PlayListsActivity
import ru.oepak22.simplemusicplayer.screen.playlists.PlayListsDialog
import ru.oepak22.simplemusicplayer.screen.playlists.PlayListsPresenter
import ru.oepak22.simplemusicplayer.screen.playlists.PlayListsView
import ru.oepak22.simplemusicplayer.widget.BaseAdapter
import ru.oepak22.simplemusicplayer.widget.EmptyRecyclerView
import javax.inject.Inject

// класс основного окна приложения
class MusicListActivity : AppCompatActivity(),
                            MusicListView,
                            PlayListsView,
                            BaseAdapter.OnItemClickListener<AudioTrack>,
                            BaseAdapter.OnItemLongClickListener<AudioTrack> {

    var currentAudioTrackTitle = ""                                                                 // имя текущего трека
    var currentAudioTrackStatus = Defines.TRACK_IS_STOPPED                                          // статус текущего трека

    private lateinit var mPlayListsPresenter: PlayListsPresenter                                    // презентер для работы с плейлистами
    lateinit var mMusicListPresenter: MusicListPresenter                                            // презентер для работы с треками
    lateinit var mAdapter: MusicListAdapter                                                         // адаптер списка треков

    private lateinit var mBottomNavigationView: BottomNavigationView                                // нижнее меню
    private lateinit var mActionBarDrawerToggle: ActionBarDrawerToggle                              // переключатель меню для открытия навигатора
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout                                    // обновлялка для списка треков
    lateinit var mDrawerLayout: DrawerLayout                                                        // навигатор бокового меню

    private var mDialog: AlertDialog? = null                                                        // диалоговое сообщение

    @Inject lateinit var mService: DataOperations

    // создание окна
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_list)
        Log.v("MusicListActivity", "onCreated")

        //
        MainApp.sAppComponent.injectMusicListActivity(this)

        // инициализация презентеров
        mMusicListPresenter = MusicListPresenter(this, mService)
        mPlayListsPresenter = PlayListsPresenter(this, mService)

        // восстановление после поворота экрана
        if (savedInstanceState != null) {
            mMusicListPresenter.restoreTrackList()
            currentAudioTrackTitle = savedInstanceState.getString("track_title")!!
            currentAudioTrackStatus = savedInstanceState.getInt("track_status")
        }

        // инициализация нижнего меню
        mBottomNavigationView = findViewById(R.id.bottom_nav_view)
        mBottomNavigationView.menu.setGroupCheckable(0, false, true)

        // установка тулбара
        val toolbar: Toolbar? = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        // инициализация навигатора, обработка открытия/закрытия
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            mDrawerLayout,
            R.string.nav_open,
            R.string.nav_close
        ) {
            override fun onDrawerOpened(drawerView: View) {
                setNavigationMenu()
            }
        }
        mActionBarDrawerToggle.syncState()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // инициализация списка треков
        val mRecyclerView: EmptyRecyclerView = findViewById(R.id.music_recycler_view)
        val mEmptyView: View = findViewById(R.id.nofiles_textview)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.mEmptyView = mEmptyView
        val list = ArrayList<AudioTrack>()
        mAdapter = MusicListAdapter(list)
        mAdapter.attachToRecyclerView(mRecyclerView)

        // инициализация обновлялки
        mSwipeRefreshLayout = findViewById(R.id.tracks_swipe_container)
        mSwipeRefreshLayout.setColorSchemeColors(
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.CYAN
        )

        // переопределение кнопки "Назад"
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mMusicListPresenter.isSelectedExist(mAdapter.mItems))
                    mMusicListPresenter.clearSelection(mAdapter.mItems)
                else finish()
            }
        }
        this.onBackPressedDispatcher.addCallback(this, callback)

        // проверка на первый запуск приложения
        if (savedInstanceState == null) checkPermission()
    }

    // возобновление работы активности
    override fun onResume() {
        super.onResume()
        Log.v("MusicListActivity", "onResume")

        // регистрация широковещательного приемника
        val intentFilter = IntentFilter()
        intentFilter.addAction(Defines.TRACK_ACTION)
        registerReceiver(mBroadcastReceiver, intentFilter)

        // обработчик нажатия на кнопки нижнего меню
        mBottomNavigationView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.navigation_add_music) openPlaylistDialog()
            else deleteConfirmDialog()
            false
        }

        // обработчик бокового меню
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle)

        // обработчики длинного и короткого нажатий на элемент списка треков
        mAdapter.mOnItemClickListener = this
        mAdapter.mOnItemLongClickListener = this

        // обработчик обновлялки списка треков
        mSwipeRefreshLayout.setOnRefreshListener {
            mMusicListPresenter.refresh(currentAudioTrackTitle, currentAudioTrackStatus)
            Handler().postDelayed({ mSwipeRefreshLayout.isRefreshing = false }, 500)
        }

        // отрисовка бокового меню
        setNavigationMenu()

        // проверка статуса службы воспроизведения и вывод списка треков
        if (!isMyServiceRunning(MusicPlayerService::class.java))
            currentAudioTrackStatus = Defines.TRACK_IS_STOPPED
        mMusicListPresenter.refresh(currentAudioTrackTitle, currentAudioTrackStatus)
    }

    // приостановка работы активности
    override fun onPause() {
        super.onPause()
        Log.v("MusicListActivity", "onPause")

        unregisterReceiver(mBroadcastReceiver)
        mSwipeRefreshLayout.setOnRefreshListener(null)
        mAdapter.mOnItemLongClickListener = null
        mAdapter.mOnItemClickListener = null
        mDrawerLayout.removeDrawerListener(mActionBarDrawerToggle)
        mBottomNavigationView.setOnNavigationItemSelectedListener(null)
    }

    // закрытие активности
    override fun onDestroy() {
        super.onDestroy()
        Log.v("MusicListActivity", "onDestroy")

        // очистка диалогового окна
        mDialog?.dismiss()
    }

    // сохранение данных перед поворотом
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.v("MusicListActivity", "onSaveInstanceState")

        mMusicListPresenter.cacheTrackList(mAdapter.mItems)
        outState.putString("track_title", currentAudioTrackTitle)
        outState.putInt("track_status", currentAudioTrackStatus)
    }

    // инициализация главного меню
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    // обработчик главного меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (mActionBarDrawerToggle.onOptionsItemSelected(item))
            return true

        when (item.itemId) {

            // кнопка настроек эквалайзера
            R.id.action_equalizer -> {
                val i = Intent(
                    this@MusicListActivity,
                    EqualizerActivity::class.java
                )
                startActivity(i)
            }

            // кнопка сортировки по наименованию трека
            R.id.sort_title -> {
                mMusicListPresenter.selectSortOrder(Defines.SORT_BY_TITLE)
                mAdapter.sortByField(Defines.SORT_BY_TITLE)
            }

            // кнопка сортировки по исполнителю трека
            R.id.sort_artist -> {
                mMusicListPresenter.selectSortOrder(Defines.SORT_BY_ARTIST)
                mAdapter.sortByField(Defines.SORT_BY_ARTIST)
            }

            // кнопка сортировки по альбому
            R.id.sort_album -> {
                mMusicListPresenter.selectSortOrder(Defines.SORT_BY_ALBUM)
                mAdapter.sortByField(Defines.SORT_BY_ALBUM)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // обработка ответа на запрос прав к хранилищу
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
            finish()
    }

    // вывод на экран списка треков
    override fun showTracks(list: ArrayList<AudioTrack>, sortOrder: Int) {
        mAdapter.changeDataSet(list)
        mAdapter.sortByField(sortOrder)
        if (!mMusicListPresenter.isSelectedExist(mAdapter.mItems))
            mBottomNavigationView.visibility = View.GONE
        else
            mBottomNavigationView.visibility = View.VISIBLE
    }

    // вывод на экран сообщения о пустом списке треков
    override fun showEmptyTracks() {
        mAdapter.clear()
        mBottomNavigationView.visibility = View.GONE
    }

    // вывод списка плейлистов в боковое меню
    override fun showPlaylists(list: ArrayList<PlayList>) {

        // обращение к боковому меню
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val menu = navigationView.menu

        // добавление подменю имен плейлистов
        val playlistsSubmenu = menu.addSubMenu(R.string.playlists)

        // добавление кнопки добавления нового плейлиста
        playlistsSubmenu.add(R.string.add_playlist).setOnMenuItemClickListener {
            addPlaylistDialog()
            return@setOnMenuItemClickListener true
        }

        // список плейлистов
        for (item in list) {
            playlistsSubmenu.add(item.mName)
                .setIcon(R.drawable.ic_baseline_music_note_24)
                .setOnMenuItemClickListener {
                    mDrawerLayout.closeDrawers()
                    mMusicListPresenter.selectPlaylist(item.mName)
                    return@setOnMenuItemClickListener true
                }
        }
    }

    // формирование бокового меню с пустым списком плейлистов
    override fun showEmptyPlaylists() {

        // обращение к боковому меню
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val menu = navigationView.menu

        // добавление подменю имен плейлистов
        val playlistsSubmenu = menu.addSubMenu(R.string.playlists)

        // добавление кнопки добавления нового плейлиста
        playlistsSubmenu.add(R.string.add_playlist).setOnMenuItemClickListener {
            addPlaylistDialog()
            return@setOnMenuItemClickListener true
        }
    }

    // сообщение об успешном добавлении нового плейлиста
    override fun showSuccess(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        setNavigationMenu()
    }

    // сообщение об ошибке при добавлении нового плейлиста
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

    // обработчик короткого нажатия на элемент списка
    override fun onItemClick(item: AudioTrack) {

        // выбор элемента
        if (mBottomNavigationView.visibility == View.VISIBLE)
            mMusicListPresenter.selectItem(item, mAdapter.mItems)

        // запуск трека для проигрывания
        else {

            // проверка трека на существования
            if (mMusicListPresenter.isTrackExist(item)) {

                mMusicListPresenter.cacheTrackList(mAdapter.mItems)
                val activityIntent = Intent(
                    this,
                    MusicPlayerActivity::class.java
                ).apply {
                    putExtra("current_track", item)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(activityIntent)
            }
            else mAdapter.remove(item)
        }
    }

    // обработчик длинного нажатия на элемент списка
    override fun onItemLongClick(item: AudioTrack) {
        mMusicListPresenter.selectItem(item, mAdapter.mItems)
    }

    // проверка прав на чтение хранилища
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(
                this,
                permissions,
                123
            )
        }
    }

    // настройка меню для бокового навигатора
    private fun setNavigationMenu() {

        // очистка списка треков от выделений и сокрытие нижнего меню
        mMusicListPresenter.clearSelection(mAdapter.mItems)
        mBottomNavigationView.visibility = View.GONE

        // очистка бокового меню
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val menu = navigationView.menu
        menu.clear()

        // настройка главного меню
        val mainSubmenu = menu.addSubMenu(R.string.main_menu)
        mainSubmenu.add(R.string.all_music)
            .setIcon(R.drawable.ic_baseline_music_all_24)
            .setOnMenuItemClickListener {
                mDrawerLayout.closeDrawers()
                mMusicListPresenter.selectPlaylist("")
                return@setOnMenuItemClickListener true
            }
        mainSubmenu.add(R.string.manage_playlists)
            .setIcon(R.drawable.ic_baseline_playlists_24)
            .setOnMenuItemClickListener {
                mDrawerLayout.closeDrawers()
                startActivity(Intent(this@MusicListActivity, PlayListsActivity::class.java))
                return@setOnMenuItemClickListener true
            }

        // проверка на наличие плейлистов
        mPlayListsPresenter.showPlaylists()
    }

    // открытие диалога выбора плейлиста
    private fun openPlaylistDialog() {
        val playListsDialog = PlayListsDialog()
        playListsDialog.show(supportFragmentManager, "PlayListsDialog")
    }

    // диалог подтверждения удаления треков из плейлиста
    private fun deleteConfirmDialog() {
        val builder = AlertDialog.Builder(this)
        with (builder) {
            setIcon(R.drawable.ic_message_warning)
            setTitle(R.string.message_warning)
            setMessage(R.string.message_sure_delete)
            setPositiveButton(R.string.message_ok) { _, _ ->
                mMusicListPresenter.delTracksFromPlaylist(mAdapter.mItems)
            }
            setNegativeButton(R.string.message_cancel) { _, _ -> }
        }
        mDialog = builder.create()
        mDialog!!.show()
    }

    // добавление трека в плейлист
    fun addTrackInPlaylist(playlistName: String) {
        Toast.makeText(
            this@MusicListActivity,
            "Successfully added",
            Toast.LENGTH_LONG
        ).show()
        mMusicListPresenter.addTracksToPlaylist(playlistName, mAdapter.mItems)
    }

    // открытие диалога указания имени нового плейлиста
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
            //show()
        }
        mDialog = builder.create()
        mDialog!!.show()
    }

    // проверка статуса музыкального сервиса
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    // приемник широковещательных сообщений от службы проигрывателя
    private val mBroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1 != null) {

                val title = (p1.getSerializableExtra(Defines.CURRENT_TRACK) as AudioTrack).mTitle
                val status = (p1.getSerializableExtra(Defines.CURRENT_TRACK) as AudioTrack).mStatus

                if (currentAudioTrackTitle != title || currentAudioTrackStatus != status)
                    mMusicListPresenter.refresh(title, status)

                currentAudioTrackTitle = title
                currentAudioTrackStatus = status
            }
        }
    }
}