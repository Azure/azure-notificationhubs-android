package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;

import com.microsoft.windowsazure.messaging.R;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Collects a set of distinct tags, in order to apply them to {@link Installation}s as they are
 * created.
 */
public class TagEnricher implements InstallationEnricher, Tagable {

    private SharedPreferences mPreferences;

    /**
     * Creates an empty TagEnricher.
     */
    public TagEnricher() {

    }

    /**
     * Creates a TagEnricher with a pre-populated set of tags to apply.
     * @param tags The initial set of tags that should be applied to future {@link Installation}s.
     */
    public TagEnricher(Context context, Collection<? extends String> tags) {
        this();
        SetPreferences(context);
        addTags(tags);
    }

    public void SetPreferences (Context context) {
        mPreferences = context.getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    @Override
    public void enrichInstallation(Installation subject) {
        subject.addTags((Collection<String>) getTags());
    }

    /**
     * Adds a single tag to this collection.
     *
     * @param tag The tag to include with this collection.
     * @return True if the provided tag was not previously associated with this collection.
     */
    @Override
    public boolean addTag(String tag) {
        return addTags(Collections.singletonList(tag));
    }

    /**
     * Adds several tags to the collection.
     *
     * @param tags The tags to include with this collection.
     * @return True if any of the provided tags had not previously been associated with this
     * Installation.
     */
    @Override
    public boolean addTags(Collection<? extends String> tags) {
        Set<String> set = new HashSet<>(tags);
        mPreferences.edit().putStringSet("tags", set).apply();
        return true;
    }

    /**
     * Deletes one tag from this collection.
     *
     * @param tag The tag that should no longer be in the collection.
     * @return True if the tag had previously been associated with this collection.
     */
    @Override
    public boolean removeTag(String tag) {
        return removeTags(Collections.singletonList(tag));
    }

    /**
     * Deletes several tags from this collection.
     *
     * @param tags The tags that should no longer be in the collection.
     * @return True if any of the tags had previously been associated with this collection.
     */
    @Override
    public boolean removeTags(Collection<? extends String> tags) {
        Set<String> set = (Set<String>) getTags();
        set.removeAll(tags);
        return addTags(set);
    }

    /**
     * Fetches the tags associated with this collection.
     *
     * @return A set of tags.
     */
    @Override
    public Iterable<String> getTags() {
        return mPreferences.getStringSet("tags", new HashSet<String>());
    }

    /**
     * Empties the collection of tags.
     */
    @Override
    public void clearTags() {
        mPreferences.edit().remove("tags").apply();
    }
}
