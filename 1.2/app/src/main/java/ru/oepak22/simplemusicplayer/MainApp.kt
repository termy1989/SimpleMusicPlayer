package ru.oepak22.simplemusicplayer

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration
import ru.oepak22.simplemusicplayer.dagger.AppComponent
import ru.oepak22.simplemusicplayer.dagger.DaggerAppComponent
import ru.oepak22.simplemusicplayer.dagger.DataModule

// класс приложения
class MainApp: Application() {

    companion object {
        lateinit var sAppComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()

        // первоначальная настройка Realm
        Realm.init(this)
        val configuration = RealmConfiguration.Builder().build()
        Realm.setDefaultConfiguration(configuration)

        sAppComponent = DaggerAppComponent.builder().dataModule(DataModule()).build()
    }
}