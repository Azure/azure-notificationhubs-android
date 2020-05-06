package com.microsoft.windowsazure.messaging.notificationhubs;

import java.util.Collection;

/**
 * Standardizes the contract for supporting tags.
 */
interface Taggable {
    /**
     * Adds a single tag to this collection.
     * @param tag The tag to include with this collection.
     * @return True if the provided tag was not previously associated with this collection.
     */
    boolean addTag(String tag);

    /**
     * Adds several tags to the collection.
     * @param tags The tags to include with this collection.
     * @return True if any of the provided tags had not previously been associated with this
     *         Installation.
     */
    boolean addTags(Collection<? extends String> tags);

    /**
     * Deletes one tag from this collection.
     * @param tag The tag that should no longer be in the collection.
     * @return True if the tag had previously been associated with this collection.
     */
    boolean removeTag(String tag);

    /**
     * Deletes several tags from this collection.
     * @param tags The tags that should no longer be in the collection.
     * @return True if any of the tags had previously been associated with this collection.
     */
    boolean removeTags(Collection<? extends String> tags);

    /**
     * Fetches the tags associated with this collection.
     * @return A set of tags.
     */
    Iterable<String> getTags();

    /**
     * Empties the collection of tags.
     */
    void clearTags();
}
