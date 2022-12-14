package ru.oepak22.simplemusicplayer.media

import android.annotation.SuppressLint
import android.app.*
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import ru.oepak22.simplemusicplayer.MainApp
import ru.oepak22.simplemusicplayer.R
import ru.oepak22.simplemusicplayer.content.AudioTrack
import ru.oepak22.simplemusicplayer.content.Defines
import ru.oepak22.simplemusicplayer.content.EqualizerSettings
import ru.oepak22.simplemusicplayer.data.DataOperations
import ru.oepak22.simplemusicplayer.utils.TextUtils
import javax.inject.Inject


// класс сервиса для проигрывания музыки
class MusicPlayerService : Service(), MusicPlayerView {

    private lateinit var mPresenter: MusicPlayerPresenter                                     // презентер проигрывателя

    private var mAudioManager: AudioManager? = null                                           // аудио-менеджер
    private var mRemoteControlResponder: ComponentName? = null                                // обработчик сигналов bluetooth
    private var isRunning = false                                                             // флаг запуска члужбы

    private lateinit var handler: Handler                                                     // обработчик потокового задания
    private lateinit var thread: HandlerThread                                                // поток проигрывателя
    private lateinit var playerRunnable: MediaPlayerRunnable                                  // тело обработчика

    private lateinit var notificationBuilder: NotificationCompat.Builder                      // уведомление
    private lateinit var notificationManager: NotificationManager                             // менеджер уведомлений

    @Inject lateinit var mService: DataOperations                                             //


    // создание сервиса
    override fun onCreate() {
        super.onCreate()
        Log.v("MUSIC SERVICE", "onCreated")

        MainApp.sAppComponent.injectMusicPlayerService(this)

        thread = HandlerThread("MusicServiceStartThread")                               // инициализация потока
        thread.start()                                                                        // старт потока
        handler = Handler(thread.looper)                                                      // инициализация обработчика потоковой задачи
        isRunning = true                                                                      // установка флага запуска сервиса
        playerRunnable = MediaPlayerRunnable()                                                // потоковая задача
        mPresenter = MusicPlayerPresenter(
            this,
            mService
        )                                                                                     // инициализация презентера
        mPresenter.init()
        initNotification()                                                                    // инициализация уведомления

        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager                       // аудио-менеджер
        mRemoteControlResponder = ComponentName(                                              // обработчик сигналов от bluetooth
            this,
            MediaDeviceReceiver::class.java
        )
        mAudioManager!!.registerMediaButtonEventReceiver(mRemoteControlResponder)             // регистрация обработчика

    }

    // удаление сервиса
    override fun onDestroy() {
        super.onDestroy()
        Log.v("MUSIC SERVICE", "onDestroy")

        // очистка обработчиков и завершение потока
        mAudioManager!!.unregisterMediaButtonEventReceiver(mRemoteControlResponder)
        handler.removeCallbacks(playerRunnable)
        thread.quitSafely()
    }

    // старт сервиса
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.v("MUSIC SERVICE", "onStartCommand $startId")

        // чтение параметров
        if (intent != null) {

            val command = intent.getIntExtra("command", 0)
            Log.v("MUSIC SERVICE", "Command: $command")

            if (intent.hasExtra("current_track"))
                mPresenter.mCurrentAudioTrack =
                    intent.getSerializableExtra("current_track")
                            as AudioTrack

            // обработка отправленной команды
            when (command) {

                // команда воспроизведения трека
                Defines.COMMAND_PLAY -> {

                    // варианты запуска трека
                    when (mPresenter.getTrackStatus()) {
                        Defines.TRACK_IS_PAUSED -> mPresenter.resume()                      // возобновление после паузы
                        Defines.TRACK_IS_STOPPED -> {
                            mPresenter.restorePlayList()                                    // получение плейлиста из базы
                            mPresenter.play()                                               // запуск нового трека
                        }
                        Defines.TRACK_IS_PLAYED -> mPresenter.pause()                       // постановка трека на паузу
                    }
                }

                // перемотка трека
                Defines.COMMAND_SEEK -> mPresenter
                    .seek(intent
                        .getIntExtra("position", 0))

                // переход к предыдущему треку
                Defines.COMMAND_PREV -> {
                    if (mPresenter.mCurrentAudioTrack!!.mStatus == Defines.TRACK_IS_STOPPED)
                        mPresenter.restorePlayList()
                    if (mPresenter.mPlayListMode == Defines.RANDOM_MODE)
                        mPresenter.random()
                    else mPresenter.prev()
                }

                // переход к следующему треку
                Defines.COMMAND_NEXT -> {
                    if (mPresenter.mCurrentAudioTrack!!.mStatus == Defines.TRACK_IS_STOPPED)
                        mPresenter.restorePlayList()
                    if (mPresenter.mPlayListMode == Defines.RANDOM_MODE)
                        mPresenter.random()
                    else mPresenter.next()
                }

                // режим проигрывания списка
                Defines.COMMAND_MODE -> {
                    mPresenter.mPlayListMode = intent
                        .getIntExtra("mode", Defines.REPEAT_MODE)
                }

                // настройка эквалайзера
                Defines.COMMAND_EQ -> {
                    mPresenter.equalizer(intent.getSerializableExtra("eq")
                            as EqualizerSettings)
                }

                // завершение работы сервиса
                Defines.COMMAND_CLOSE -> closeService()
            }
        }

        // очистка обработчика потокового задания
        handler.removeCallbacks(playerRunnable)

        // периодический запуск обработчика потокового задания
        handler.postDelayed(playerRunnable, 100)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    // вывод сообщения об ошибке
    override fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    // завершение работы сервиса
    private fun closeService() {
        isRunning = false
        mPresenter.stop()
        sendBroadcast(mPresenter.getIntent())
        mPresenter.close()
        stopForeground(true)
        stopSelf()
    }

    /*** управление уведомлениями ***/

    // инициализация уведомления
    private fun initNotification() {

        // инициализация канала уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "MUSIC_SERVICE",
                "Music Player Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "Service for playing music"
            notificationChannel.setShowBadge(true)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // построение уведомления
        notificationBuilder = NotificationCompat.Builder(
            this@MusicPlayerService,
            "MUSIC_SERVICE"
        ).apply {
            setSmallIcon(R.drawable.notify_icon)
            setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.notify_icon))
            setSilent(true)
            //setAutoCancel(true)
            //setOngoing(true)
            priority = NotificationCompat.PRIORITY_HIGH
        }
    }

    // отображение процесса воспроизведения трека
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun updateNotification() {

        // уведомление беззвучное
        notificationBuilder.setSilent(true)

        // заголовок  - наименование трека
        notificationBuilder.setContentTitle(TextUtils.boldText(mPresenter.getTrackName()))

        // текст - исполнитель и время проигрывания
        notificationBuilder.setContentText(mPresenter.getTrackAuthor() + " ("
                + TextUtils.convertToMMSS(mPresenter.getCurrentPosition().toString())
                + " / " + TextUtils.convertToMMSS(mPresenter.getDuration().toString()) + ")")

        // очистка кнопок
        notificationBuilder.clearActions()

        // кнопка смены режима воспроизведения
        val orderIntent = Intent(
            this@MusicPlayerService,
            MusicPlayerService::class.java
        )
        orderIntent.action = Defines.TRACK_ACTION
        orderIntent.putExtra("command", Defines.COMMAND_MODE)
        when (mPresenter.mPlayListMode) {
            Defines.REPEAT_MODE -> orderIntent.putExtra(
                "mode",
                Defines.REPEAT_ONE_MODE
            )
            Defines.REPEAT_ONE_MODE -> orderIntent.putExtra(
                "mode",
                Defines.RANDOM_MODE
            )
            Defines.RANDOM_MODE -> orderIntent.putExtra(
                "mode",
                Defines.REPEAT_MODE
            )
        }
        val orderPending = PendingIntent.getService(
            this@MusicPlayerService,
            0,
            orderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // кнопка предыдущего трека
        val prevIntent = Intent(
            this@MusicPlayerService,
            MusicPlayerService::class.java
        )
        prevIntent.action = Defines.TRACK_ACTION
        prevIntent.putExtra("command", Defines.COMMAND_PREV)
        val prevPending = PendingIntent.getService(
            this@MusicPlayerService,
            1,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // кнопка паузы/воспроизведения
        val playIntent = Intent(
            this@MusicPlayerService,
            MusicPlayerService::class.java
        )
        playIntent.action = Defines.TRACK_ACTION
        playIntent.putExtra("command", Defines.COMMAND_PLAY)
        /*playIntent.putExtra(
            "current_track",
            mPresenter.mCurrentAudioTrack
        )*/
        val playPending = PendingIntent.getService(
            this@MusicPlayerService,
            2,
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // кнопка следующего трека
        val nextIntent = Intent(
            this@MusicPlayerService,
            MusicPlayerService::class.java
        )
        nextIntent.action = Defines.TRACK_ACTION
        nextIntent.putExtra("command", Defines.COMMAND_NEXT)
        val nextPending = PendingIntent.getService(
            this@MusicPlayerService,
            3,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // кнопка завершения работы
        val cancelIntent = Intent(
            this@MusicPlayerService,
            MusicPlayerService::class.java
        )
        cancelIntent.action = Defines.TRACK_ACTION
        cancelIntent.putExtra("command", Defines.COMMAND_CLOSE)
        val cancelPending = PendingIntent.getService(
            this@MusicPlayerService,
            4,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // добавление кнопка смены режима воспроизведения
        when (mPresenter.mPlayListMode) {

            Defines.REPEAT_MODE -> notificationBuilder.addAction(
                R.drawable.ic_notify_repeat_24,
                "Repeat",
                orderPending
            )

            Defines.REPEAT_ONE_MODE -> notificationBuilder.addAction(
                R.drawable.ic_notify_repeat_one_24,
                "Repeat_one",
                orderPending
            )

            Defines.RANDOM_MODE -> notificationBuilder.addAction(
                R.drawable.ic_notify_random_24,
                "Random",
                orderPending)
        }

        // добавление кнопки предыдущего трека
        notificationBuilder.addAction(
            R.drawable.ic_notify_skip_prev_24,
            "Prev",
            prevPending
        )

        // добавление кнопки паузы/воспроизведения
        if (mPresenter.getTrackStatus() == Defines.TRACK_IS_PLAYED)
            notificationBuilder.addAction(
                R.drawable.ic_notify_pause_24,
                "Pause",
                playPending
            )
        else
            notificationBuilder.addAction(
                R.drawable.ic_notify_play_24,
                "Play",
                playPending
            )

        // добавление кнопки следующего трека
        notificationBuilder.addAction(
            R.drawable.ic_notify_skip_next_24,
            "Next",
            nextPending
        )

        // добавление кнопки завершения сервиса
        notificationBuilder.addAction(
            R.drawable.ic_notify_cancel_24,
            "Cancel",
            cancelPending
        )

        // установка стиля уведомления
        notificationBuilder.setStyle(androidx.media.app.NotificationCompat
            .MediaStyle()
            .setShowActionsInCompactView(1, 2, 3))

        // старт уведомления
        startForeground(1, notificationBuilder.build())
    }

    /*** обработчик потокового задания ***/

    inner class MediaPlayerRunnable : Runnable {

        // отправка широковещательной информации о воспроизведении
        override fun run() {
            if (isRunning) {
                sendBroadcast(mPresenter.getIntent())                                       // отправка широковещательной информации о работе проигрывателя
                updateNotification()                                                        // отображение изменений в уведомлении
                handler.postDelayed(
                    this,
                    100
                )                                                                           // повтор задачи каждые 100мс
            }
        }
    }
}