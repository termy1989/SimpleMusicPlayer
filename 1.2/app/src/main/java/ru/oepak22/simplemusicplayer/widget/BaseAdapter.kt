package ru.oepak22.simplemusicplayer.widget

import android.annotation.SuppressLint
import android.view.View
import android.view.View.OnLongClickListener
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<VH : RecyclerView.ViewHolder, T>(items: ArrayList<T>) : RecyclerView.Adapter<VH>() {

    val mItems: ArrayList<T> = ArrayList()
    private var mRecyclerView: EmptyRecyclerView? = null
    var mOnItemClickListener: OnItemClickListener<T>? = null
    var mOnItemLongClickListener: OnItemLongClickListener<T>? = null

    init {
        mItems.addAll(items)
    }

    interface OnItemClickListener<T> {
        fun onItemClick(item: T)
    }

    interface OnItemLongClickListener<T> {
        fun onItemLongClick(item: T)
    }

    private val mInternalClickListener = View.OnClickListener { view: View ->
        if (mOnItemClickListener != null) {
            val position = view.tag as Int
            val item = mItems[position]
            mOnItemClickListener!!.onItemClick(item)
        }
    }

    private val mInternalLongClickListener = OnLongClickListener { view: View ->
        if (mOnItemLongClickListener != null) {
            val position = view.tag as Int
            val item = mItems[position]
            mOnItemLongClickListener!!.onItemLongClick(item)
        }
        true
    }

    fun attachToRecyclerView(recyclerView: EmptyRecyclerView) {
        mRecyclerView = recyclerView
        mRecyclerView!!.adapter = this
        refreshRecycler()
    }

    /*open fun setOnItemClickListener(onItemClickListener: OnItemClickListener<T>?) {
        mOnItemClickListener = onItemClickListener
    }

    open fun senOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener<T>?) {
        mOnItemLongClickListener = onItemLongClickListener
    }*/

    fun add(value: T) {
        mItems.add(value)
        refreshRecycler()
    }

    fun remove(value: T) {
        mItems.remove(value)
        refreshRecycler()
    }

    fun changeDataSet(values: ArrayList<T>) {
        mItems.clear()
        mItems.addAll(values)
        refreshRecycler()
    }

    fun clear() {
        mItems.clear()
        refreshRecycler()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshRecycler() {
        notifyDataSetChanged()
        mRecyclerView?.checkIfEmpty()
    }

    fun getItem(position: Int): T {
        return mItems[position]
    }

    @CallSuper
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.itemView.tag = position
        holder.itemView.setOnClickListener(mInternalClickListener)
        holder.itemView.setOnLongClickListener(mInternalLongClickListener)
    }

    override fun getItemCount(): Int {
        return mItems.size
    }
}