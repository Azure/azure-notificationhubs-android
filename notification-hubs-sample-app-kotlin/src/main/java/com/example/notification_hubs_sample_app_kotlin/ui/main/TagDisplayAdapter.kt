package com.example.notification_hubs_sample_app_kotlin.ui.main

import android.view.LayoutInflater
import com.example.notification_hubs_sample_app_kotlin.R
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TagDisplayAdapter(viewModel: SetupViewModel) : RecyclerView.Adapter<TagDisplayAdapter.ViewHolder?>() {
    private var mTags: List<String>? = null
    private val mViewModel: SetupViewModel = viewModel

    /**
     * Called when RecyclerView needs a new [androidx.recyclerview.widget.RecyclerView.ViewHolder] of the given type to represent
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
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.tag_row_item, parent, false)
        return ViewHolder(v)
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the [androidx.recyclerview.widget.RecyclerView.ViewHolder.itemView] to reflect the item at the given
     * position.
     *
     *
     * Note that unlike [ListView], RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the `position` parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use [androidx.recyclerview.widget.RecyclerView.ViewHolder.getAdapterPosition] which will
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
        holder.tagText.text = mTags!![position]
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return if (mTags == null) 0 else mTags!!.size;
    }

    fun setTags(tags: List<String>?) {
        mTags = tags
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mTagText: TextView = itemView.findViewById(R.id.tag_entry_value) as TextView
        private val mDeleteButton: ImageButton = itemView.findViewById(R.id.tag_delete_button) as ImageButton
        val tagText: TextView
            get() = mTagText

        val deleteButton: ImageButton
            get() = mDeleteButton

        init {
            mDeleteButton.setOnClickListener { mViewModel.removeTag(mTagText.text.toString()) }
        }
    }

}