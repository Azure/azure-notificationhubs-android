package com.example.notification_hubs_sample_app_java.ui.main;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notification_hubs_sample_app_java.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationDisplayAdapter extends RecyclerView.Adapter<NotificationDisplayAdapter.ViewHolder> {

    private List<Intent> mNotifications;
    private NotificationClickListener mClickListener;
    private final String mDefaultTitle;
    private final String mDefaultMessage;

    public NotificationDisplayAdapter(String defaultTitle, String defaultMessage) {
        this(defaultTitle, defaultMessage, new ArrayList<Intent>(0));
    }

    public NotificationDisplayAdapter(String defaultTitle, String defaultMessage, List<Intent> initialList) {
        mNotifications = initialList;
        mClickListener = message -> {
            // Intentionally Left Blank
        };
        mDefaultTitle = defaultTitle;
        mDefaultMessage = defaultMessage;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list_item, parent, false);
        return new ViewHolder(v);
    }

    public void setNotifications(List<Intent> notifications) {
        mNotifications = notifications;
        notifyDataSetChanged();
    }

    public void setClickListener(NotificationClickListener listener) {
        mClickListener = listener;
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Intent entry = mNotifications.get(position);

        holder.mTitle.setText("Title Place Holder");
        holder.mBody.setText("Body Place Holder");

        holder.mDataCardinality.setText(String.valueOf(entry.getExtras().size()));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final TextView mBody;
        private final TextView mDataCardinality;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.titleValue);
            mBody = (TextView) itemView.findViewById(R.id.bodyValue);
            mDataCardinality = (TextView) itemView.findViewById(R.id.dataCardinalityValue);
            itemView.setOnClickListener(v -> {
                Intent clicked = NotificationDisplayAdapter.this.mNotifications.get(getAdapterPosition());
                NotificationDisplayAdapter.this.mClickListener.onNotificationClicked(clicked);
            });
        }
    }

    public interface NotificationClickListener {
        void onNotificationClicked(Intent message);
    }
}
