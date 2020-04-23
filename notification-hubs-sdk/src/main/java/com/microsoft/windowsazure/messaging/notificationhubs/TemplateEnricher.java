package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;

import com.microsoft.windowsazure.messaging.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Collects a set of distinct templates, in order to apply them to {@link Installation}s as they are
 * created.
 */
public class TemplateEnricher implements InstallationEnricher {

    private static final String PREFERENCE_KEY = "templates";
    private SharedPreferences mPreferences;

    /**
     * Creates an empty TagEnricher.
     */
    public TemplateEnricher() {

    }

    /**
     * Creates a TagEnricher with a pre-populated set of templates to apply.
     * @param templates The initial set of templates that should be applied to future {@link Installation}s.
     */
    public TemplateEnricher(Context context, Collection<? extends InstallationTemplate> templates) {
        this();
        setPreferences(context);
        addTemplates(templates);
    }

    @Override
    public void enrichInstallation(Installation subject) {
        subject.addTemplates(getTemplatesSet());
    }

    /**
     * Get a set of templates.
     *
     * @return A set of templates.
     */
    private Set<InstallationTemplate> getTemplatesSet() {
        Set<String> preferencesSet = mPreferences.getStringSet(PREFERENCE_KEY, new HashSet<>());
        if (preferencesSet == null) {
            return new HashSet<>();
        }
        return deserializeInstallationTemplateToJson(preferencesSet);
    }

    /**
     * Save templates to shared preference.
     */
    void setPreferences(Context context) {
        mPreferences = context.getSharedPreferences(String.valueOf(R.string.installation_enrichment_file_key), Context.MODE_PRIVATE);
    }

    /**
     * Adds a single template to this collection.
     *
     * @param template The template to include with this collection.
     * @return True if the provided template was not previously associated with this collection.
     */
    public boolean addTemplate(InstallationTemplate template) {
        return addTemplates(Collections.singletonList(template));
    }

    /**
     * Adds several templates to the collection.
     *
     * @param templates The templates to include with this collection.
     * @return True if any of the provided templates had not previously been associated with this
     * Installation.
     */

    public boolean addTemplates(Collection<? extends InstallationTemplate> templates) {
        Set<InstallationTemplate> set = getTemplatesSet();
        set.addAll(templates);
        Set<String> serializedTemplatesSet = new HashSet<>();
        for (InstallationTemplate template: set) {
            serializedTemplatesSet.add(serializeInstallationTemplateToJson(template));
        }
        mPreferences.edit().putStringSet(PREFERENCE_KEY, serializedTemplatesSet).apply();
        return true;
    }

    /**
     * Deletes one template from this collection.
     *
     * @param template The template that should no longer be in the collection.
     * @return True if the template had previously been associated with this collection.
     */
    public boolean removeTemplate(InstallationTemplate template) {
        return removeTemplates(Collections.singletonList(template));
    }

    /**
     * Deletes several templates from this collection.
     *
     * @param templates The templates that should no longer be in the collection.
     * @return True if any of the templates had previously been associated with this collection.
     */
    public boolean removeTemplates(Collection<? extends InstallationTemplate> templates) {
        Set<InstallationTemplate> set = getTemplatesSet();
        set.removeAll(templates);
        Set<String> serializedTemplatesSet = new HashSet<>();
        for (InstallationTemplate template: set) {
            serializedTemplatesSet.add(serializeInstallationTemplateToJson(template));
        }
        mPreferences.edit().putStringSet(PREFERENCE_KEY, serializedTemplatesSet).apply();
        return true;
    }

    /**
     * Fetches the templates associated with this collection.
     *
     * @return A set of templates.
     */
    public Iterable<InstallationTemplate> getTemplates() {
        return getTemplatesSet();
    }

    /**
     * Empties the collection of templates.
     */
    public void clearTemplates() {
        mPreferences.edit().remove(PREFERENCE_KEY).apply();
    }

    /**
     * Serialize InstallationTemplate to JSONObject.
     *
     * @param installationTemplate The templates that should no longer be in the collection.
     * @return serialized templateObject.
     */
    private static String serializeInstallationTemplateToJson(InstallationTemplate installationTemplate) {
        JSONObject templateObject = new JSONObject();
        try {
            templateObject.put("body", installationTemplate.getBody());
            JSONObject headers = new JSONObject();
            Iterator<Map.Entry<String, String>> headersIterators = installationTemplate.getHeaders().iterator();
            while (headersIterators.hasNext()) {
                Map.Entry<String, String> headerKey = headersIterators.next();
                headers.put(headerKey.getKey(), headerKey.getValue());
            }
            templateObject.put("headers", headers);
            JSONArray tagsArray = new JSONArray();
            installationTemplate.getTags().forEach(tag -> tagsArray.put(tag));
            templateObject.put("tags", tagsArray);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return templateObject.toString();
    }

    /**
     * Deserialize JSONObject to InstallationTemplate.
     *
     * @param installationTemplatesSet The templates that should no longer be in the collection.
     * @return A set of deserialized templates.
     */
    private static Set<InstallationTemplate> deserializeInstallationTemplateToJson(Set<String> installationTemplatesSet) {
        Set<InstallationTemplate> templates = new HashSet<>();
        try {
            for (String preferenceString : installationTemplatesSet) {
                JSONObject preference = new JSONObject(preferenceString);
                InstallationTemplate template = new InstallationTemplate();
                template.setBody(preference.getString("body"));
                JSONArray tags = preference.getJSONArray("tags");
                for (int tagKey = 0; tagKey < tags.length(); tagKey++) {
                    template.addTag(tags.getString(tagKey));
                }
                JSONObject headers = preference.getJSONObject("headers");
                Iterator<String> headersIterators = headers.keys();
                while (headersIterators.hasNext()) {
                    String headerKey = headersIterators.next();
                    template.setHeader(headerKey, headers.getString(headerKey));
                }
                templates.add(template);
            }
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
        return templates;
    }
}
