package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;

import com.microsoft.windowsazure.messaging.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
     * Creates an empty TemplateEnricher.
     */
    public TemplateEnricher() {

    }

    /**
     * Creates a TemplateEnricher with a pre-populated set of templates to apply.
     * @param context application context
     * @param templates The initial set of templates that should be applied to future {@link Installation}s.
     */
    public TemplateEnricher(Context context, Map<String, InstallationTemplate> templates) {
        this();
        setPreferences(context);
        addTemplates(templates);
    }

    @Override
    public void enrichInstallation(Installation subject) {
        subject.addTemplates(getSharedPreferenceTemplates());
    }

    /**
     * Get a set of templates.
     *
     * @return A map of templates.
     */
    private Map<String, InstallationTemplate> getSharedPreferenceTemplates() {
        Set<String> preferencesSet = mPreferences.getStringSet(PREFERENCE_KEY, new HashSet<>());
        if (preferencesSet == null) {
            return new HashMap<>();
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
     * @param templateName Name of template
     * @param template The template to include with this collection.
     * @return True if the provided template was not previously associated with this collection.
     */
    public boolean addTemplate(String templateName, InstallationTemplate template) {

        return addTemplates(Collections.singletonMap(templateName, template));
    }

    /**
     * Adds several templates to the collection.
     *
     * @param templates The collection of named templates to include with this collection.
     * @return True if any of the provided templates had not previously been associated with this
     * Installation.
     */

    public boolean addTemplates(Map<String, InstallationTemplate> templates) {
        Map<String, InstallationTemplate> sharedPreferenceTemplates = getSharedPreferenceTemplates();
        sharedPreferenceTemplates.putAll(templates);
        Set<String> serializedTemplatesSet = new HashSet<>();
        for (Map.Entry<String, InstallationTemplate> template: sharedPreferenceTemplates.entrySet()) {
            serializedTemplatesSet.add(serializeInstallationTemplateToJson(template.getKey(), template.getValue()));
        }
        mPreferences.edit().putStringSet(PREFERENCE_KEY, serializedTemplatesSet).apply();
        return true;
    }

    /**
     * Deletes one template from this collection by name.
     *
     * @param templateName The name of template that should no longer be in the collection.
     * @return True if the template had previously been associated with this collection.
     */
    public boolean removeTemplate(String templateName) {
        return removeTemplates(Collections.singletonList(templateName));
    }

    /**
     * Deletes several templates from this collection.
     *
     * @param templates The templates name collection that should no longer be in the collection.
     * @return True if any of the templates had previously been associated with this collection.
     */
    public boolean removeTemplates(List<String> templates) {
        Map<String, InstallationTemplate> sharedPreferenceTemplates = getSharedPreferenceTemplates();
        sharedPreferenceTemplates.keySet().removeAll(templates);
        Set<String> serializedTemplatesSet = new HashSet<>();
        for (Map.Entry<String, InstallationTemplate> installation: sharedPreferenceTemplates.entrySet()) {
            serializedTemplatesSet.add(serializeInstallationTemplateToJson(installation.getKey(), installation.getValue()));
        }
        mPreferences.edit().putStringSet(PREFERENCE_KEY, serializedTemplatesSet).apply();
        return true;
    }

    /**
     * Fetches the template by name
     * @param templateName Name of template
     * @return Return template associated with name
     */
    public InstallationTemplate getTemplate(String templateName) {
        Map<String, InstallationTemplate> templates = getSharedPreferenceTemplates();
        return templates.get(templateName);
    }

    /**
     * Fetches the templates associated with this collection.
     *
     * @return A set of templates.
     */
    public Iterable<Map.Entry<String, InstallationTemplate>> getTemplates() {
        return getSharedPreferenceTemplates().entrySet();
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
     * @param name Name of template
     * @param installationTemplate The templates that should no longer be in the collection.
     * @return serialized templateObject.
     */
    private static String serializeInstallationTemplateToJson(String name, InstallationTemplate installationTemplate) {
        JSONObject templateObject = new JSONObject();
        try {
            templateObject.put("name", name);
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
    private static Map<String, InstallationTemplate> deserializeInstallationTemplateToJson(Set<String> installationTemplatesSet) {
        Map<String, InstallationTemplate> templates = new HashMap<>();
        try {
            for (String preferenceString : installationTemplatesSet) {
                JSONObject preference = new JSONObject(preferenceString);
                InstallationTemplate template = new InstallationTemplate();
                String name = preference.getString("name");
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
                templates.put(name, template);
            }
        } catch(JSONException ex) {
            ex.printStackTrace();
        }
        return templates;
    }
}
