package com.microsoft.windowsazure.messaging.notificationhubs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;

public class InstallationTemplate {
    private String mBody;
    private Set<String> mTags = new HashSet<>();
    private Map<String, String> mHeaders = new HashMap<>();

    public String getBody() {
        return mBody;
    }

    public void setBody(String value) {
        mBody = value;
    }

    public void addTag(String tag) {
        mTags.add(tag);
    }

    public void addTags(List<String> tags) {
        mTags.addAll(tags);
    }

    public void removeTag(String tag) {
        mTags.remove(tag);
    }

    public void removeTags(List<String> tags) {
        mTags.removeAll(tags);
    }

    public Iterable<String> getTags() {
        return mTags;
    }

    public void clearTags() {
        mTags = new HashSet<>();
    }

    public void setHeader(String name, String value) {
        mHeaders.put(name, value);
    }
    public void removeHeader(String name) {
        mHeaders.remove(name);
    }

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
}
