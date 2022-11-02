package ru.oepak22.simplemusicplayer.media

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.oepak22.simplemusicplayer.R
import ru.oepak22.simplemusicplayer.content.AudioTrack
import ru.oepak22.simplemusicplayer.content.Defines
import ru.oepak22.simplemusicplayer.utils.TextUtils


// класс активности плеера
class MusicPlayerActivity : AppCompatActivity() {

    var playListMode = Defines.REPEAT_MODE                                  // режим проигрывания плейлиста
    private lateinit var currentAudioTrack: AudioTrack                      // текущий трек

    private lateinit var titleTv: TextView                                  // название трека
    private lateinit var currentTimeTv: TextView                            // текущее время проигрывания
    private lateinit var totalTimeTv: TextView                              // длительность трека

    private lateinit var pausePlay: ImageView                               // кнопка "пауза/проигрывание"
    private lateinit var nextBtn: ImageView                                 // кнопка "следующий трек"
    private lateinit var previousBtn: ImageView                             // кнопка "предыдущий трек"
    private lateinit var orderBtn: ImageView                                // кнопка "режим проигрывания плейлиста"

    private lateinit var seekBar: SeekBar                                   // полоса перемотки

    // создание активности
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)
        Log.v("MusicPlayerActivity", "onCreate")

        // инициализация компонентов активности
        titleTv = findViewById(R.id.song_title)
        titleTv.isSelected = true
        currentTimeTv = findViewById(R.id.current_time)
        totalTimeTv = findViewById(R.id.total_time)
        pausePlay = findViewById(R.id.pause_play)
        nextBtn = findViewById(R.id.next)
        previousBtn = findViewById(R.id.previous)
        orderBtn = findViewById(R.id.order)
        seekBar = findViewById(R.id.seek_bar)

        // инициализация текущего трека
        if (savedInstanceState == null) {
            currentAudioTrack = intent.getSerializableExtra("current_track") as AudioTrack
            if (currentAudioTrack.mStatus == Defines.TRACK_IS_STOPPED) {
                val serviceIntent = Intent(this,
                    MusicPlayerService::class.java).apply {
                        putExtra("command", Defines.COMMAND_PLAY)
                        putExtra("current_track", currentAudioTrack)
                    }
                startService(serviceIntent)
            }
        }
        else {
            currentAudioTrack = savedInstanceState.getSerializable("saved_track") as AudioTrack
            playListMode = savedInstanceState.getInt("saved_mode", Defines.REPEAT_MODE)
            outputTrackInfo()
        }

        // регистрация широковещательного приемника
        val intentFilter = IntentFilter()
        intentFilter.addAction(Defines.TRACK_ACTION)
        registerReceiver(mBroadcastReceiver, intentFilter)
    }

    // возобновление работы активности
    override fun onResume() {
        super.onResume()
        Log.v("MusicPlayerActivity", "onResume")

        // кнопка воспроизведение/пауза
        pausePlay.setOnClickListener {
            val intent = Intent(this,
                MusicPlayerService::class.java).apply {
                    putExtra("command", Defines.COMMAND_PLAY)
                    putExtra("current_track", currentAudioTrack)
                }
            startService(intent)
        }

        // кнопка "следующий трек"
        nextBtn.setOnClickListener {
            val intent = Intent(this, MusicPlayerService::class.java)
            intent.putExtra("command", Defines.COMMAND_NEXT)
            if (currentAudioTrack.mStatus == Defines.TRACK_IS_STOPPED)
                intent.putExtra("current_track", currentAudioTrack)
            startService(intent)
        }

        // кнопка "предыдущий трек"
        previousBtn.setOnClickListener {
            val intent = Intent(this, MusicPlayerService::class.java)
            intent.putExtra("command", Defines.COMMAND_PREV)
            if (currentAudioTrack.mStatus == Defines.TRACK_IS_STOPPED)
                intent.putExtra("current_track", currentAudioTrack)
            startService(intent)
        }

        // кнопка "порядок воспроизведения"
        orderBtn.setOnClickListener {
            if (currentAudioTrack.mStatus != Defines.TRACK_IS_STOPPED) {
                val intent = Intent(this, MusicPlayerService::class.java)
                intent.putExtra("command", Defines.COMMAND_MODE)
                when (playListMode) {
                    Defines.REPEAT_MODE -> intent.putExtra("mode", Defines.REPEAT_ONE_MODE)
                    Defines.REPEAT_ONE_MODE -> intent.putExtra("mode", Defines.RANDOM_MODE)
                    Defines.RANDOM_MODE -> intent.putExtra("mode", Defines.REPEAT_MODE)
                }
                startService(intent)
            }
            else
                Toast.makeText(
                    this,
                    "Playing music is not running",
                    Toast.LENGTH_SHORT
                ).show()
        }

        // перемотка
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (currentAudioTrack.mStatus != Defines.TRACK_IS_STOPPED) {
                        val intent = Intent(
                            this@MusicPlayerActivity,
                            MusicPlayerService::class.java
                        )
                        intent.putExtra("command", Defines.COMMAND_SEEK)
                        intent.putExtra("position", progress)
                        startService(intent)
                    }
                    else seekBar.progress = 0
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    // постановка активности на паузу
    override fun onPause() {
        super.onPause()
        Log.v("MusicPlayerActivity", "onPause")

        // обнуление обработчиков нажатий
        pausePlay.setOnClickListener(null)
        nextBtn.setOnClickListener(null)
        previousBtn.setOnClickListener(null)
        seekBar.setOnSeekBarChangeListener(null)
    }

    // уничтожение активности
    override fun onDestroy() {
        super.onDestroy()
        Log.v("MusicPlayerActivity", "onDestroy")

        // отключение широковещательного приемника
        unregisterReceiver(mBroadcastReceiver)
    }

    // сохранение данных перед поворотом
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.v("MusicPlayerActivity", "onSaveInstanceState")
        currentAudioTrack.mStatus = Defines.TRACK_IS_STOPPED
        outState.putSerializable("saved_track", currentAudioTrack)
        outState.putInt("saved_mode", playListMode)
    }

    // вывод информации о текущем треке
    private fun outputTrackInfo() {

        // режим проигрывания плейлиста
        when (playListMode) {
            Defines.REPEAT_MODE -> orderBtn.setImageResource(R.drawable.ic_player_repeat_24)
            Defines.REPEAT_ONE_MODE -> orderBtn.setImageResource(R.drawable.ic_player_repeat_one_24)
            Defines.RANDOM_MODE -> orderBtn.setImageResource(R.drawable.ic_player_random_24)
        }

        // имя и путь текущего трека
        if (titleTv.text != currentAudioTrack.mTitle)
            titleTv.text = currentAudioTrack.mTitle

        // статус трека
        if (currentAudioTrack.mStatus == Defines.TRACK_IS_PLAYED)
            pausePlay.setImageResource(R.drawable.ic_player_pause_circle_outline_24)
        else
            pausePlay.setImageResource(R.drawable.ic_player_play_circle_outline_24)

        // начало и конец полосы перемотки
        seekBar.progress = currentAudioTrack.mPosition
        seekBar.max = currentAudioTrack.mDuration

        // начало и конец трека
        currentTimeTv.text = TextUtils.convertToMMSS(seekBar.progress.toString() + "")
        totalTimeTv.text = TextUtils.convertToMMSS(seekBar.max.toString() + "")
    }

    // приемник широковещательных сообщений от службы проигрывателя
    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {

            if (p1 != null) {

                // получение информации
                playListMode = p1.getIntExtra(Defines.PLAYLIST_MODE, Defines.REPEAT_MODE)
                currentAudioTrack = p1.getSerializableExtra(Defines.CURRENT_TRACK) as AudioTrack

                // вывод информации
                outputTrackInfo()
            }
        }
    }
}