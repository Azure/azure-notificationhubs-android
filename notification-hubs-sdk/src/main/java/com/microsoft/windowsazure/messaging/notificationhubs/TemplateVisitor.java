package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import android.content.SharedPreferences;

import com.microsoft.windowsazure.messaging.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collects a set of distinct templates, in order to apply them to {@link Installation}s as they are
 * created.
 */
class TemplateVisitor implements InstallationVisitor {

    private static final String PREFERENCE_KEY = "templates";
    private final SharedPreferences mPreferences;

    /**
     * Creates an empty TemplateVisitor.
     */
    public TemplateVisitor(Context context) {
        this(context.getSharedPreferences(String.valueOf(R.string.installation_enrichment_file_key), Context.MODE_PRIVATE));
    }

    TemplateVisitor(SharedPreferences sharedPreferences) {
        mPreferences = sharedPreferences;
    }

    /**
     * Creates a TemplateVisitor with a pre-populated set of templates to apply.
     * @param context application context
     * @param templates The initial set of templates that should be applied to future {@link Installation}s.
     */
    public TemplateVisitor(Context context, Map<String, InstallationTemplate> templates) {
        this(context);
        setTemplates(templates);
    }

    @Override
    public void visitInstallation(Installation subject) {
        subject.setTemplates(getSharedPreferenceTemplates());
    }

    /**
     * Get a set of templates.
     *
     * @return A map of templates.
     */
    private Map<String, InstallationTemplate> getSharedPreferenceTemplates() {
        Set<String> preferencesSet = mPreferences.getStringSet(PREFERENCE_KEY, new HashSet<String>());
        if (preferencesSet == null) {
            return new HashMap<>();
        }

        try {
            return deserializeInstallationTemplateFromJson(preferencesSet);
        }
        catch (JSONException ex) {
            throw new RuntimeException("Unable to deserialize installation template", ex);
        }
    }

    /**
     * Adds or updates single template in this collection.
     *
     * @param templateName Name of template
     * @param template The template to include or update in this collection.
     */
    public void setTemplate(String templateName, InstallationTemplate template) {
        setTemplates(Collections.singletonMap(templateName, template));
    }

    /**
     * Adds or updates several templates in the collection.
     *
     * @param templates The collection of named templates to include or update in this collection.
     * Installation.
     */
    public void setTemplates(Map<String, InstallationTemplate> templates) {
        Map<String, InstallationTemplate> sharedPreferenceTemplates = getSharedPreferenceTemplates();
        sharedPreferenceTemplates.putAll(templates);
        Set<String> serializedTemplatesSet = new HashSet<>();
        for (Map.Entry<String, InstallationTemplate> template: sharedPreferenceTemplates.entrySet()) {
            serializedTemplatesSet.add(serializeInstallationTemplateToJson(template.getKey(), template.getValue()));
        }
        mPreferences.edit().putStringSet(PREFERENCE_KEY, serializedTemplatesSet).apply();
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
        return InstallationTemplate.serialize(name, installationTemplate).toString();
    }

    /**
     * Deserialize JSONObject to InstallationTemplate.
     *
     * @param installationTemplatesSet The templates that should no longer be in the collection.
     * @return A set of deserialized templates.
     * @throws JSONException When there's a schema-mismatch of the object to populate, and what
     * appears in the serialized form of the template.
     */
    private static Map<String, InstallationTemplate> deserializeInstallationTemplateFromJson(Set<String> installationTemplatesSet) throws JSONException {
        Map<String, InstallationTemplate> templates = new HashMap<>();

        for (String preferenceString : installationTemplatesSet) {
            JSONObject preference = new JSONObject(preferenceString);
            String name = preference.getString("name");
            InstallationTemplate template = InstallationTemplate.deserialize(preference);
            templates.put(name, template);
        }

        return templates;
    }
}
