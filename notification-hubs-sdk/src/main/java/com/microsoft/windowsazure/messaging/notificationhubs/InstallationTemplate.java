package com.microsoft.windowsazure.messaging.notificationhubs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.Nullable;

public class InstallationTemplate {
    private String mBody;
    private Set<String> mTags = new HashSet<>();
    private Map<String, String> mHeaders = new HashMap<>();

    public InstallationTemplate() {

    }
    
    /**
     * Get template body.
     *
     * @return string of body.
     */
    public String getBody() {
        return mBody;
    }

    /**
     * Set template body.
     * @param value is a body payload.
     */
    public void setBody(String value) {
        mBody = value;
    }

    /**
     * Add tag to template.
     *
     * @param tag
     */
    public void addTag(String tag) {
        mTags.add(tag);
    }

    /**
     * Add list of tags to template.
     *
     * @param tags list of tags.
     */
    public void addTags(List<String> tags) {
        mTags.addAll(tags);
    }

    /**
     * Remove tag from template.
     *
     * @param tag
     */
    public void removeTag(String tag) {
        mTags.remove(tag);
    }

    /**
     * Remove list of tags from template.
     *
     * @param tags list of tags for removing.
     */
    public void removeTags(List<String> tags) {
        mTags.removeAll(tags);
    }

    /**
     * Get all tags from template.
     *
     * @return all tags from template
     */
    public Iterable<String> getTags() {
        return mTags;
    }

    /**
     * Remove all tags from template.
     */
    public void clearTags() {
        mTags = new HashSet<>();
    }

    /**
     * Set header to template.
     */
    public void setHeader(String name, String value) {
        mHeaders.put(name, value);
    }

    /**
     * Remove header from template.
     */
    public void removeHeader(String name) {
        mHeaders.remove(name);
    }

    /**
     * Get all headers from template.
     */
    Iterable<Map.Entry<String,String>> getHeaders() {
        return mHeaders.entrySet();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof InstallationTemplate)) return false;

        InstallationTemplate castedObj = (InstallationTemplate) obj;

        return mBody.equals(castedObj.mBody) && mTags.equals(castedObj.mTags) && mHeaders.equals(castedObj.mHeaders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mBody, mTags, mHeaders);
    }
}
