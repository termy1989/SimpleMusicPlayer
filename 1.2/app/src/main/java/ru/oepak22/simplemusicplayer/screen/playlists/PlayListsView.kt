package ru.oepak22.simplemusicplayer.screen.playlists

import ru.oepak22.simplemusicplayer.content.PlayList

// интерфейс для презентера списка плейлистов
interface PlayListsView {
    fun showPlaylists(list : ArrayList<PlayList>)               // вывод содержимого на экран
    fun showEmptyPlaylists()                                    // вывод пустого пространства на экран
    fun showSuccess(msg: String)                                // сообщение об успешной операции
    fun showError(msg: String)                                  // сообщение об ошибке при операции
}