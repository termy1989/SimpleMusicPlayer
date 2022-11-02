package ru.oepak22.simplemusicplayer.widget

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.oepak22.simplemusicplayer.R
import ru.oepak22.simplemusicplayer.screen.tracks.MusicListActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val intent = Intent(this, MusicListActivity::class.java)
        startActivity(intent)
        finish()
    }
}