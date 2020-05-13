package com.microsoft.windowsazure.messaging.notificationhubs;

import androidx.test.filters.SmallTest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SmallTest
public class InstallationTemplateTest {

    @Test
    public void InstallationTemplateSerialize() throws JSONException {
        String templateName = "testTemplate";
        String templateBody = "{\"data\":{\"message\":\"Notification Hub test notification\"}}";
        InstallationTemplate template = new InstallationTemplate();
        template.setBody(templateBody);

        JSONObject serializedTemplate = InstallationTemplate.serialize(templateName, template);

        assertEquals(templateBody, serializedTemplate.getString("body"));
    }

    @Test
    public void InstallationTemplateDeserialize() throws JSONException {
        String templateBody = "{\"data\":{\"message\":\"Notification Hub test notification\"}}";
        JSONObject serializedTemplate = new JSONObject();
        serializedTemplate.put("body", templateBody);

        InstallationTemplate deserializedTemplate = InstallationTemplate.deserialize(serializedTemplate);

        assertEquals(templateBody, deserializedTemplate.getBody());
    }

    @Test
    public void SerializeThenDeserializeNotChangingObject() throws JSONException {
        String templateName = "testTemplate";
        String templateBody = "{\"data\":{\"message\":\"Notification Hub test notification\"}}";
        InstallationTemplate template = new InstallationTemplate();
        template.setBody(templateBody);

        JSONObject serializedTemplate = InstallationTemplate.serialize(templateName, template);

        InstallationTemplate deserializedTemplate = InstallationTemplate.deserialize(serializedTemplate);

        assertEquals(template.getBody(), deserializedTemplate.getBody());
    }
}
