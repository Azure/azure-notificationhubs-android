package com.example.notification_hubs_test_app_kotlin.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.notification_hubs_test_app_kotlin.R
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationMessage
import java.util.ArrayList


class NotificationDisplayAdapter @kotlin.jvm.JvmOverloads constructor(initialList: List<NotificationMessage> = ArrayList(0)) : RecyclerView.Adapter<NotificationDisplayAdapter.ViewHolder?>() {
    private var mNotifications: List<NotificationMessage>
    private var mClickListener: NotificationClickListener

    /**
     * Called when RecyclerView needs a new [ViewHolder] of the given type to represent
     * an item.
     *
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     *
     *
     * The new ViewHolder will be used to display items of the adapter using
     * [.onBindViewHolder]. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary [View.findViewById] calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see .getItemViewType
     * @see .onBindViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.notification_list_item, parent, false)
        return ViewHolder(v)
    }

    fun setNotifications(notifications: List<NotificationMessage>) {
        mNotifications = notifications
        notifyDataSetChanged()
    }

    fun setClickListener(listener: NotificationClickListener) {
        mClickListener = listener
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the [ViewHolder.itemView] to reflect the item at the given
     * position.
     *
     *
     * Note that unlike [ListView], RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the `position` parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use [ViewHolder.getAdapterPosition] which will
     * have the updated adapter position.
     *
     *
     * Override [.onBindViewHolder] instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry: NotificationMessage = mNotifications[position]
        holder.mTitle.text = entry.title
        holder.mBody.text = entry.body
        holder.mDataCardinality.text = entry.data.size.toString()
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mTitle: TextView = itemView.findViewById(R.id.titleValue) as TextView
        val mBody: TextView = itemView.findViewById(R.id.bodyValue) as TextView
        val mDataCardinality: TextView = itemView.findViewById(R.id.dataCardinalityValue) as TextView

        init {
            itemView.setOnClickListener {
                val clicked: NotificationMessage = mNotifications[adapterPosition]
                mClickListener.onNotificationClicked(clicked)
            }
        }
    }

    interface NotificationClickListener {
        fun onNotificationClicked(message: NotificationMessage?)
    }

    init {
        mNotifications = initialList
        mClickListener = object : NotificationClickListener {
            override fun onNotificationClicked(message: NotificationMessage?) {

            }
        }
    }

    override fun getItemCount(): Int {
        return mNotifications.size
    }
}