package ru.oepak22.simplemusicplayer.content

// константы
interface Defines {

    companion object {

        const val TRACK_ACTION = "ru.oepak22.simplemusicplayer.track_action"            // идентификатор широковещания

        const val CURRENT_TRACK = "ru.oepak22.simplemusicplayer.current_track"          // теккущий трек
        const val PLAYLIST_MODE = "ru.oepak22.simplemusicplayer.playlist_mode"          // передача режима проигрывания

        const val TRACK_IS_STOPPED = 0                                                  // трек остановлен
        const val TRACK_IS_PLAYED = 1                                                   // трек проигрывается
        const val TRACK_IS_PAUSED = 2                                                   // трек на паузе

        const val COMMAND_PLAY = 3                                                      // команда - включение трека
        const val COMMAND_PREV = 4                                                      // команда - предыдущий трек
        const val COMMAND_NEXT = 5                                                      // команда - следующий трек
        const val COMMAND_SEEK = 6                                                      // команда - перемотка трека
        const val COMMAND_MODE = 7                                                      // команда - смена режима воспроизведения
        const val COMMAND_EQ = 8                                                        // команда - настройка эквалайзера
        const val COMMAND_CLOSE= 9                                                      // команда - закрытие проигрывателя

        const val REPEAT_MODE = 10                                                      // режим - проигрывание плейлиста по порядку
        const val REPEAT_ONE_MODE = 11                                                  // режим - проигрывание одного и того же трека
        const val RANDOM_MODE = 12                                                      // режим - случайный выбор трека для проигрывания

        const val SORT_BY_TITLE = 13                                                    // флаг - сортировка по имени трека
        const val SORT_BY_ARTIST = 14                                                   // флаг - сортировка по исполнителю трека
        const val SORT_BY_ALBUM = 15                                                    // флаг - сортировка по альбому
    }
}