package ru.oepak22.simplemusicplayer.screen.playlists

import android.content.Context
import ru.oepak22.simplemusicplayer.content.PlayList
import ru.oepak22.simplemusicplayer.data.DataService

// класс презентера для работы с плейлистами
class PlayListsPresenter(initView : PlayListsView, initService : DataService) {

    private var mView = initView
    private val mService = initService

    // добавление нового плейлиста
    fun addNewPlaylist(name: String)
    {
        if (mService.addNewPlaylist(name))
            mView.showSuccess("Successfully added")
        else
            mView.showError("Can't add new playlist. May be it is already existed or empty")

    }

    // изменение выбранного плейлиста
    fun editSelectedPlaylist(oldName: String, newName: String) {
        if (mService.editSelectedPlaylist(oldName, newName))
            mView.showSuccess("Successfully edited")
        else
            mView.showError("Can't edit playlist. May be new name already existed or empty")
    }

    // удаление выбранных плейлистов
    fun deleteSelectedPlaylist(selectedPlaylists: ArrayList<PlayList>) {

        // удаление
        if (mService.delSelectedPlaylist(selectedPlaylists)) {

            // фиксация текущего плейлиста
            val currentList = mService.restoreCurrentPlaylist(mView as Context)

            // если текущий плейлист среди удаленных - он меняется на общий список
            for (list in selectedPlaylists) {
                if (list.mName == currentList)
                    mService.saveCurrentPlaylist(mView as Context, "")
            }

            mView.showSuccess("Successfully deleted")
        }
        else
            mView.showError("Can't delete selected playlist. May be it isn't existed")
    }

    // вывод списка плейлистов
    fun showPlaylists() {
        val list = mService.showAllPlaylists()
        if (list == null) mView.showEmptyPlaylists()
        else mView.showPlaylists(list)
    }
}