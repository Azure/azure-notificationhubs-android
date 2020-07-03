package com.microsoft.windowsazure.messaging.notificationhubs;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Wraps a record of a device, for use resolving future requests of broadcast notifications.
 */
public class Installation implements Taggable {
    private String mPushChannel;
    private Set<String> mTags;
    private Map<String, InstallationTemplate> mTemplates;
    private String mInstallationId;
    private Date mExpiration;

    public Installation() {
        mTags = new HashSet<String>();
        mTemplates = new HashMap<String, InstallationTemplate>();
    }

    /**
     * Fetches the unique code that will be used to identify this device.
     * @return A unique code that will be used to identify this device.
     */
    public String getPushChannel() {
        return mPushChannel;
    }

    /**
     * Sets the unique code that will be used to identify the device originating this {@link Installation}.
     * @param pushChannel A unique code that identifies this device. Should be generated by your
     *                    operating system, or the library that is being used to centralize polling
     *                    for new notifications.
     */
    public void setPushChannel(String pushChannel) {
        mPushChannel = pushChannel;
    }

    /**
     * Fetches the unique identifier used by the backend tracking this record of a Device.
     * @return A unique identifier for this installation record.
     */
    public String getInstallationId() {
        return mInstallationId;
    }

    /**
     * Sets the unique identifier that should be used by the backend to track the record of this
     * Device.
     * @param id The unique identifer to associate with the record of this device.
     */
    void setInstallationId(String id) {
        mInstallationId = id;
    }

    /**
     * Adds a single tag to this Installation.
     * @param tag The tag to include with this Installation.
     * @return True if the provided tag was not previously associated with this Installation.
     */
    public boolean addTag(String tag) {
        return mTags.add(tag);
    }

    /**
     * Adds several tags to this Installation.
     * @param tags The tags to include with this Installation.
     * @return True if any of the provided tags had not previously been associated with this
     *         Installation.
     */
    public boolean addTags(Collection<? extends String> tags) {
        return mTags.addAll(tags);
    }

    /**
     * Fetches the tags associated with this collection.
     * @return A set of tags.
     */
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

    /**
     * Remove specific tag from this Installation.
     * @param tag The tag that should no longer be in the collection.
     * @return True if the tag had previously been associated with this collection.
     */
    public boolean removeTag(String tag) {
        return mTags.remove(tag);
    }

    /**
     * Deletes several tags from this collection.
     * @param tags The tags that should no longer be in the collection.
     * @return True if any of the tags had previously been associated with this collection.
     */
    public boolean removeTags(Collection<? extends String> tags) {
        return mTags.removeAll(tags);
    }

    /**
     * Adds or updates several templates in this Installation.
     * @param templates The templates to include or update in this Installation.
     */
    public void setTemplates(Map<String, InstallationTemplate> templates) { mTemplates.putAll(templates); }

    public Map<String, InstallationTemplate> getTemplates() { return mTemplates; }

    /**
     * Fetches the moment in time when this {@link Installation} will no longer be deemed valid.
     * @return The point in time where this {@link Installation} should no longer be considered valid.
     */
    public Date getExpiration() {
        return mExpiration;
    }

    /**
     * Sets the moment in time when this {@link Installation} will no longer be deemed valid.
     * @param expiration The point in time where this {@link Installation} should no longer be considered valid.
     */
    public void setExpiration(Date expiration) {
        mExpiration = expiration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Installation that = (Installation) o;
        return mPushChannel.equals(that.mPushChannel) &&
                mTags.equals(that.mTags) &&
                mTemplates.equals(that.mTemplates) &&
                mInstallationId.equals(that.mInstallationId) &&
                mExpiration.equals(that.mExpiration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPushChannel, mTags, mTemplates, mInstallationId, mExpiration);
    }
}
