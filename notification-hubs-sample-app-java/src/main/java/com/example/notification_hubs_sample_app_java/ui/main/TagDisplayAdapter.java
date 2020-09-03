package com.example.notification_hubs_sample_app_java.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notification_hubs_sample_app_java.R;

import java.util.List;

public class TagDisplayAdapter extends RecyclerView.Adapter<TagDisplayAdapter.ViewHolder> {

    private List<String> mTags;
    private final SetupViewModel mViewModel;

    public TagDisplayAdapter(SetupViewModel viewModel) {
        mViewModel = viewModel;
    }

    /**
     * Called when RecyclerView needs a new {@link androidx.recyclerview.widget.RecyclerView.ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder, int)
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_row_item, parent, false);

        return new ViewHolder(v);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link androidx.recyclerview.widget.RecyclerView.ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link androidx.recyclerview.widget.RecyclerView.ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(androidx.recyclerview.widget.RecyclerView.ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getTagText().setText(mTags.get(position));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mTags == null ? 0 : mTags.size();
    }

    public void setTags(List<String> tags) {
        mTags = tags;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTagText;
        private final ImageButton mDeleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTagText = (TextView) itemView.findViewById(R.id.tag_entry_value);
            mDeleteButton = (ImageButton) itemView.findViewById(R.id.tag_delete_button);
            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewModel.removeTag(mTagText.getText().toString());
                }
            });
        }

        public TextView getTagText() {
            return mTagText;
        }

        public ImageButton getDeleteButton() {
            return mDeleteButton;
        }
    }
}
