package ru.oepak22.simplemusicplayer.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import java.util.concurrent.TimeUnit

// класс для работы с текстом
object TextUtils {

    // "жирный" текст
    fun boldText(text: String): Spannable {
        val sb: Spannable = SpannableString(text)
        sb.setSpan(
            StyleSpan(Typeface.BOLD), 0, text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return sb
    }

    // перевод теста во время
    fun convertToMMSS(duration: String): String {
        val millis = java.lang.Long.parseLong(duration)
        return String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1))
    }
}