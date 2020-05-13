package com.microsoft.windowsazure.messaging.notificationhubs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InstallationTemplate {
    private String mBody;
    private Set<String> mTags = new HashSet<>();
    private Map<String, String> mHeaders = new HashMap<>();
    
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
     * Serialize InstallationTemplate to JSONObject.
     *
     * @param name Name of template
     * @param installationTemplate The templates that should no longer be in the collection.
     * @return serialized templateObject.
     */
    public static JSONObject serialize(String name, InstallationTemplate installationTemplate) {
        JSONObject templateObject = new JSONObject();
        try {
            templateObject.put("name", name);
            templateObject.put("body", installationTemplate.getBody());
            JSONObject headers = new JSONObject();
            Iterable<Map.Entry<String, String>> headersIterators = installationTemplate.getHeaders();
            for(Map.Entry<String, String> header: headersIterators){
                headers.put(header.getKey(), header.getValue());
            }
            templateObject.put("headers", headers);
            JSONArray tagsArray = new JSONArray();
            for (String tag : installationTemplate.getTags()) {
                tagsArray.put(tag);
            }
            templateObject.put("tags", tagsArray);
        } catch (JSONException ex) {
            // Investigating the possible sources of JSONException, it seems the only time it is
            // thrown is when a Number is invalid. Because we are exclusively serializing strings
            // above, this exception _should_ never be thrown.
            throw new RuntimeException("Invalid template, unable to serialize", ex);
        }
        return templateObject;
    }

    /**
     * Deserialize JSONObject to InstallationTemplate.
     *
     * @param installationTemplate Template to deserialize
     * @return Deserialized template.
     * @throws JSONException When there's a schema-mismatch of the object to populate, and what
     * appears in the serialized form of the template.
     */
    public static InstallationTemplate deserialize(JSONObject installationTemplate) throws JSONException {
        InstallationTemplate template = new InstallationTemplate();
        template.setBody(installationTemplate.getString("body"));
        JSONArray tags = installationTemplate.getJSONArray("tags");
        for (int tagKey = 0; tagKey < tags.length(); tagKey++) {
            template.addTag(tags.getString(tagKey));
        }
        JSONObject headers = installationTemplate.getJSONObject("headers");
        Iterator<String> headersIterators = headers.keys();
        while (headersIterators.hasNext()) {
            String headerKey = headersIterators.next();
            template.setHeader(headerKey, headers.getString(headerKey));
        }

        return template;
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
