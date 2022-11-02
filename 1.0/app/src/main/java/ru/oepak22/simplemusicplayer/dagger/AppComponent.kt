package ru.oepak22.simplemusicplayer.dagger

import dagger.Component
import ru.oepak22.simplemusicplayer.media.MusicPlayerService
import ru.oepak22.simplemusicplayer.screen.equalizer.EqualizerActivity
import ru.oepak22.simplemusicplayer.screen.playlists.PlayListsActivity
import ru.oepak22.simplemusicplayer.screen.playlists.PlayListsDialog
import ru.oepak22.simplemusicplayer.screen.tracks.MusicListActivity
import javax.inject.Singleton

//интерфейс для внедрения зависимостей в активности и задачи
@Singleton
@Component(modules = [DataModule::class])
interface AppComponent {
    fun injectMusicListActivity(musicListActivity: MusicListActivity)
    fun injectPlayListsActivity(playListsActivity: PlayListsActivity)
    fun injectPlayListsDialog(playListsDialog: PlayListsDialog)
    fun injectMusicPlayerService(musicPlayerService: MusicPlayerService)
    fun injectEqualizerActivity(equalizerActivity: EqualizerActivity)
}