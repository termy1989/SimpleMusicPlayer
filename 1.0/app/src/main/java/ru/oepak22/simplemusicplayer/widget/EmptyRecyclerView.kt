package ru.oepak22.simplemusicplayer.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.RecyclerView

class EmptyRecyclerView : RecyclerView {

    var mEmptyView: View? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun checkIfEmpty() {
        if (adapter!!.itemCount > 0) showRecycler()
        else showEmptyView()
    }

    @VisibleForTesting
    fun showRecycler() {
        if (mEmptyView != null) mEmptyView!!.visibility = GONE
        else visibility = VISIBLE
    }

    @VisibleForTesting
    fun showEmptyView() {
        if (mEmptyView != null) mEmptyView!!.visibility = VISIBLE
        else visibility = GONE
    }
}