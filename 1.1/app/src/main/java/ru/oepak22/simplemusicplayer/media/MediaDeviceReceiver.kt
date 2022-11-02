package ru.oepak22.simplemusicplayer.media

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import ru.oepak22.simplemusicplayer.content.Defines

// класс ресивера-обработчика сигналов от bluetooth-устройства
class MediaDeviceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // действие при отпускании кнопки - null
        val event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT) as KeyEvent?
        if (event == null || event.action != KeyEvent.ACTION_DOWN) return
        Log.v("Key Code", event.keyCode.toString())

        // проверка, запущен ли музыкальный сервис
        val manager = context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {

            // сервис запущен
            if (MusicPlayerService::class.java.name == service.service.className) {

                // обработка сигнала от bluetooth-устройства
                when (event.keyCode) {

                    // Play
                    KeyEvent.KEYCODE_MEDIA_PAUSE,
                    KeyEvent.KEYCODE_MEDIA_PLAY,
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                    KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK -> {
                        val i = Intent(context, MusicPlayerService::class.java)
                        i.putExtra("command", Defines.COMMAND_PLAY)
                        context.startService(i)
                    }

                    // Next
                    KeyEvent.KEYCODE_MEDIA_NEXT,
                    KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD,
                    KeyEvent.KEYCODE_MEDIA_STEP_FORWARD -> {
                        val i = Intent(context, MusicPlayerService::class.java)
                        i.putExtra("command", Defines.COMMAND_NEXT)
                        context.startService(i)
                    }

                    // Prev
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                    KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD,
                    KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD -> {
                        val i = Intent(context, MusicPlayerService::class.java)
                        i.putExtra("command", Defines.COMMAND_PREV)
                        context.startService(i)
                    }
                }
            }
        }
    }
}