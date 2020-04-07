package com.microsoft.windowsazure.messaging.notificationhubs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Collects a set of distinct tags, in order to apply them to {@link Installation}s as they are
 * created.
 */
public class TagEnricher implements InstallationEnricher, Tagable {

    public final Set<String> mTags;

    /**
     * Creates an empty TagEnricher.
     */
    public TagEnricher() {
        mTags = new HashSet<String>();
    }

    /**
     * Creates a TagEnricher with a pre-populated set of tags to apply.
     * @param tags The initial set of tags that should be applied to future {@link Installation}s.
     */
    public TagEnricher(Collection<? extends String> tags) {
        this();
        mTags.addAll(tags);
    }

    @Override
    public void enrichInstallation(Installation subject) {
        subject.addTags(mTags);
    }

    /**
     * Adds a single tag to this collection.
     *
     * @param tag The tag to include with this collection.
     * @return True if the provided tag was not previously associated with this collection.
     */
    @Override
    public boolean addTag(String tag) {
        return mTags.add(tag);
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
        return mTags.addAll(tags);
    }

    /**
     * Deletes one tag from this collection.
     *
     * @param tag The tag that should no longer be in the collection.
     * @return True if the tag had previously been associated with this collection.
     */
    @Override
    public boolean removeTag(String tag) {
        return mTags.remove(tag);
    }

    /**
     * Deletes several tags from this collection.
     *
     * @param tags The tags that should no longer be in the collection.
     * @return True if any of the tags had previously been associated with this collection.
     */
    @Override
    public boolean removeTags(Collection<? extends String> tags) {
        return mTags.removeAll(tags);
    }

    /**
     * Fetches the tags associated with this collection.
     *
     * @return A set of tags.
     */
    @Override
    public Iterable<String> getTags() {
        return mTags;
    }

    /**
     * Empties the collection of tags.
     */
    @Override
    public void clearTags() {
        mTags.clear();
    }
}
