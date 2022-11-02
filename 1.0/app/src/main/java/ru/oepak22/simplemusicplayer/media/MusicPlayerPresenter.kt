package ru.oepak22.simplemusicplayer.media

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.util.Log
import ru.oepak22.simplemusicplayer.content.AudioTrack
import ru.oepak22.simplemusicplayer.content.Defines
import ru.oepak22.simplemusicplayer.content.EqualizerSettings
import ru.oepak22.simplemusicplayer.data.DataService
import java.io.File


// класс презентера проигрывателя
class MusicPlayerPresenter(initView: MusicPlayerView, initService: DataService) {

    var mPlayListMode = Defines.REPEAT_MODE                                                      // режим проигрывания плейлиста
    var mCurrentAudioTrack: AudioTrack? = null                                                   // текущий трек
    private var mCurrentPlayList: ArrayList<AudioTrack> = ArrayList()                            // текущий плейлист
    private val mediaPlayer = MediaPlayer()                                                      // экземпляр проигрывателя
    private var mView = initView                                                                 // интерфейс презентера
    private var mService = initService

    private lateinit var equalizer: Equalizer                                                    // эквалайзер
    private lateinit var bassBoost: BassBoost                                                    // басы
    private lateinit var reverb: PresetReverb                                                    // ревербератор

    // инициализация презентера
    fun init() {

        // при завершении проигрывания трека - следующий трек в зависимости от режима
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setOnCompletionListener {
            Log.v("MUSIC SERVICE", "Complete: " + mCurrentAudioTrack!!.mTitle)
            mCurrentAudioTrack!!.mStatus = Defines.TRACK_IS_STOPPED

            when (mPlayListMode) {
                Defines.REPEAT_MODE -> nextAuto()
                Defines.REPEAT_ONE_MODE -> play()
                Defines.RANDOM_MODE -> random()
            }
        }

        // обработчик ошибок (для повторного запуска трека)
        mediaPlayer.setOnErrorListener { _: MediaPlayer, what: Int, extra: Int ->

            Log.e("MUSIC SERVICE ERROR", String.format("Error(%s%s)", what, extra))

            if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED)
                mediaPlayer.reset()

            else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN)
                mediaPlayer.reset()

            play()
            return@setOnErrorListener true
        }

        equalizer = Equalizer(0, mediaPlayer.audioSessionId)
        bassBoost = BassBoost(0, mediaPlayer.audioSessionId)
        reverb = PresetReverb(0, mediaPlayer.audioSessionId)

        val settings = mService.restoreEqualizerSettings(mView as Context)
        if (settings != null) equalizer(settings)
        mediaPlayer.setAuxEffectSendLevel(1.0f)
    }

    // применение настроек эквалайзера
    fun equalizer(settings: EqualizerSettings) {

        // эквалайзер активирован
        if (settings.isEqualizerEnabled) {

            // включение эквалайзера
            equalizer.enabled = true
            Log.v("MUSIC SERVICE", "Equalizer is enabled")

            // предустановки эквалайзера
            if (settings.presetPos == 0) {
                for (bandIdx in 0 until equalizer.numberOfBands)
                    equalizer.setBandLevel(
                        bandIdx.toShort(),
                        settings.seekbarpos[bandIdx].toShort()
                    )
            } else equalizer.usePreset((settings.presetPos - 1).toShort())

            // частоты эквалайзера
            for (i in 0..4) {
                val equalizerBandIndex: Short = i.toShort()
                equalizer.setBandLevel(equalizerBandIndex, settings.seekbarpos[i].toShort())
                Log.v(
                    "MUSIC SERVICE",
                    "Band " + i + ": Freq " + equalizer.getBandLevel(i.toShort())
                )
            }

            try {

                // басы
                bassBoost.enabled = true
                bassBoost.setStrength((settings.bassStrength * 1000 / 19).toShort())
                Log.v("MUSIC SERVICE", "Bass strength: " + bassBoost.roundedStrength)

                // ревербератор
                reverb.enabled = true
                reverb.preset = (settings.reverbPreset * 6 / 19).toShort()
                Log.v("MUSIC SERVICE", "Reverb preset: " + reverb.preset)
            }
            catch (ex: Exception) {
                Log.e("MUSIC SERVICE ERROR", ex.message.toString())
                mView.showError(ex.message.toString())
            }
        }
        else {
            Log.v("MUSIC SERVICE", "Equalizer is disabled")
            equalizer.enabled = false
            bassBoost.enabled = false
            reverb.enabled = false
        }
    }

    // новое воспроизведение трека
    fun play() {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(mCurrentAudioTrack!!.mPath)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { mp -> mp.start() }
            mCurrentAudioTrack!!.mStatus = Defines.TRACK_IS_PLAYED
            Log.v("MUSIC SERVICE", "New play: " + mCurrentAudioTrack!!.mTitle)
        }
        catch (ex: Exception) {
            mCurrentAudioTrack!!.mStatus = Defines.TRACK_IS_STOPPED
            Log.e("MUSIC SERVICE ERROR", ex.message.toString())
            mView.showError(ex.message.toString())
        }
    }

    // возобновление воспроизведения трека
    fun resume() {
        mediaPlayer.start()
        mCurrentAudioTrack!!.mStatus = Defines.TRACK_IS_PLAYED
        Log.v("MUSIC SERVICE", "Resume: " + mCurrentAudioTrack!!.mTitle)
    }

    // постановка трека на паузу
    fun pause() {
        mediaPlayer.pause()
        mCurrentAudioTrack!!.mStatus = Defines.TRACK_IS_PAUSED
        Log.v("MUSIC SERVICE", "Pause: " + mCurrentAudioTrack!!.mTitle)
    }

    // перемотка трека
    fun seek(position: Int) {
        mediaPlayer.seekTo(position)
    }

    // остановка воспроизведения
    fun stop() {
        mediaPlayer.seekTo(0)
        mediaPlayer.stop()
        mCurrentAudioTrack!!.mStatus = Defines.TRACK_IS_STOPPED
    }

    // завершение работы презентера
    fun close() {
        mCurrentAudioTrack = null
        mCurrentPlayList.clear()
        equalizer.release()
        bassBoost.release()
        reverb.release()
        mediaPlayer.release()
    }

    // автоматическое перелистывание треков вперед
    private fun nextAuto() {

        var index = 0

        // поиск отыгравшего трека в плейлисте
        for (track in mCurrentPlayList) {

            // трек найден - фиксация порядкового номера
            if (track.mTitle == mCurrentAudioTrack!!.mTitle)
                index = mCurrentPlayList.indexOf(track)
        }

        // плейлист не закончен
        if ((index + 1) < mCurrentPlayList.size) {

            // переход к следующему треку
            mCurrentAudioTrack = mCurrentPlayList[index + 1]
            Log.v("MUSIC SERVICE", "Next track: " + mCurrentAudioTrack!!.mTitle)

            // проверка следующего трека на существование
            val file = File(mCurrentAudioTrack!!.mPath)

            // проигрывание трека, если он еще существует
            if (file.exists()) play()

            // иначе - переход к следующему треку
            else nextAuto()
        }
    }

    // переход к следующему треку
    fun next() {

        var index = 0

        // поиск отыгравшего трека в плейлисте - фиксация порядкового номера
        for (track in mCurrentPlayList) {
            if (track.mTitle == mCurrentAudioTrack!!.mTitle)
                index = mCurrentPlayList.indexOf(track)
        }

        // плейлист не закончен - переход к следующему треку
        if (index != mCurrentPlayList.size - 1) {
            mCurrentAudioTrack = mCurrentPlayList[index + 1]
            Log.v("MUSIC SERVICE", "Next track: " + mCurrentAudioTrack!!.mTitle)
        }

        // конец плейлиста - переход к началу плейлиста
        else {
            mCurrentAudioTrack = mCurrentPlayList[0]
            Log.v("MUSIC SERVICE", "Go to start: " + mCurrentAudioTrack!!.mTitle)
        }

        // попытка воспроизведения
        play()
    }

    // переход к предыдущему треку
    fun prev() {

        var index = 0

        // поиск отыгравшего трека в плейлисте - фиксация порядкового номера
        for (track in mCurrentPlayList) {
            if (track.mTitle == mCurrentAudioTrack!!.mTitle)
                index = mCurrentPlayList.indexOf(track)
        }

        // плейлист не в начале - переход к предыдущему треку
        if (index != 0) {
            mCurrentAudioTrack = mCurrentPlayList[index - 1]
            Log.v("MUSIC SERVICE", "Prev track: " + mCurrentAudioTrack!!.mTitle)
        }

        // плейлист в начале - переход к концу плейлиста
        else {
            mCurrentAudioTrack = mCurrentPlayList[mCurrentPlayList.size - 1]
            Log.v("MUSIC SERVICE", "Go to finish: " + mCurrentAudioTrack!!.mTitle)
        }

        // попытка воспроизведения
        play()
    }

    // случайный выбор трека для воспроизведения
    fun random() {

        // случайный выбор номера трека для воспроизведения
        val index = (0 until mCurrentPlayList.size).random()
        mCurrentAudioTrack = mCurrentPlayList[index]
        play()
    }

    // составление информации для широковещательной отправки
    fun getIntent(): Intent {
        val intent = Intent(Defines.TRACK_ACTION)
        if (mCurrentAudioTrack != null) {
            mCurrentAudioTrack!!.mPosition = mediaPlayer.currentPosition
            mCurrentAudioTrack!!.mDuration = mediaPlayer.duration
            if (mediaPlayer.isPlaying)
                mCurrentAudioTrack!!.mStatus = Defines.TRACK_IS_PLAYED

            intent.putExtra(Defines.CURRENT_TRACK, mCurrentAudioTrack)
            intent.putExtra(Defines.PLAYLIST_MODE, mPlayListMode)
        }
        return intent
    }

    // получение плейлиста списком
    fun restorePlayList() {
        mCurrentPlayList = mService.restoreTrackList()!!
    }

    // имя трека
    fun getTrackName(): String {
        return mCurrentAudioTrack!!.mTitle
    }

    // автор трека
    fun getTrackAuthor(): String {
        return mCurrentAudioTrack!!.mArtist
    }

    // статус текущего трека
    fun getTrackStatus(): Int {
        return mCurrentAudioTrack!!.mStatus
    }

    // текущее положение воспроизведения
    fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    // продолжительность трека
    fun getDuration(): Int {
        return mediaPlayer.duration
    }
}