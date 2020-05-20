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
class TagVisitor implements InstallationVisitor, Taggable {

    private static final String PREFERENCE_KEY = "tags";
    private SharedPreferences mPreferences;

    /**
     * Creates an TagEnricher, with the tags previously associated with this application.
     * @param context Application context
     */
    public TagVisitor(Context context) {
        mPreferences = context.getSharedPreferences(context.getString(R.string.installation_enrichment_file_key), Context.MODE_PRIVATE);
    }

    private Set<String> getTagsSet() {
        return new HashSet<String>(mPreferences.getStringSet(PREFERENCE_KEY, new HashSet<String>()));
    }

    @Override
    public void visitInstallation(Installation subject) {
        subject.addTags(getTagsSet());
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
        Set<String> set = getTagsSet();
        set.addAll(tags);
        mPreferences.edit().putStringSet(PREFERENCE_KEY, set).apply();
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
        Set<String> set = getTagsSet();
        if(set.removeAll(tags)) {
            mPreferences.edit().putStringSet(PREFERENCE_KEY, set).apply();
            return true;
        }
        return false;
    }

    /**
     * Fetches the tags associated with this collection.
     *
     * @return A set of tags.
     */
    @Override
    public Iterable<String> getTags() {
        return getTagsSet();
    }

    /**
     * Empties the collection of tags.
     */
    @Override
    public void clearTags() {
        mPreferences.edit().remove(PREFERENCE_KEY).apply();
    }
}
