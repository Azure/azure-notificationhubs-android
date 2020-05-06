package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TemplateEnricherTests {
    private Context context = getInstrumentation().getTargetContext();
    String templateName;
    InstallationTemplate template = new InstallationTemplate();
    private Map<String, InstallationTemplate> templateMap;

    public TemplateEnricherTests() {
        templateName = "templateName";
        template.addTags(Stream.of("tag1", "tag2", "tag3").collect(Collectors.toList()));
        template.setHeader("header1", "value1");
        template.setBody("body1");
        templateMap = new HashMap<String, InstallationTemplate>(){{ put(templateName, template); }};
    }

    @Test
    public void templateEnricherAddTemplate() {
        TemplateEnricher templateEnricher = new TemplateEnricher();
        templateEnricher.setPreferences(context);

        assertTrue(templateEnricher.addTemplate(templateName, template));
        Iterable<Map.Entry<String, InstallationTemplate>> expectedTemplateSet = templateMap.entrySet();
        assertEquals(expectedTemplateSet, templateEnricher.getTemplates());
        assertEquals(template, templateEnricher.getTemplate(templateName));
    }

    @Test
    public void templateEnricherAddTemplates () {
        String templateName2 = "templateName2";
        InstallationTemplate template2 = new InstallationTemplate();
        template2.addTags(Stream.of("tag4", "tag5", "tag6").collect(Collectors.toList()));
        template2.setHeader("header2", "value2");
        template2.setBody("body2");
        templateMap.put(templateName2, template2);

        TemplateEnricher templateEnricher = new TemplateEnricher();
        templateEnricher.setPreferences(context);

        assertTrue(templateEnricher.addTemplates(templateMap));
        Iterable<Map.Entry<String, InstallationTemplate>> expectedTemplateSet = templateMap.entrySet();
        assertEquals(expectedTemplateSet, templateEnricher.getTemplates());
        assertEquals(template, templateEnricher.getTemplate(templateName));
        assertEquals(template2, templateEnricher.getTemplate(templateName2));
    }

    @Test
    public void templateEnricherRemoveTemplate () {
        TemplateEnricher templateEnricher = new TemplateEnricher();
        templateEnricher.setPreferences(context);

        assertTrue(templateEnricher.addTemplates(templateMap));
        Iterable<Map.Entry<String, InstallationTemplate>> expectedTemplateSet = templateMap.entrySet();
        assertEquals(expectedTemplateSet, templateEnricher.getTemplates());
        templateEnricher.removeTemplate(templateName);
        assertEquals(templateMap.size() - 1,
                ((Set<Map.Entry<String, InstallationTemplate>>) templateEnricher.getTemplates()).size());
        assertNull(templateEnricher.getTemplate(templateName));
    }

    @Test
    public void templateEnricherRemoveTemplates () {
        String templateName2 = "templateName2";
        InstallationTemplate template2 = new InstallationTemplate();
        template2.addTags(Stream.of("tag4", "tag5", "tag6").collect(Collectors.toList()));
        template2.setHeader("header2", "value2");
        template2.setBody("body2");
        templateMap.put(templateName2, template2);

        String templateName3 = "templateName3";
        InstallationTemplate template3 = new InstallationTemplate();
        template3.addTags(Stream.of("tag7", "tag8", "tag9").collect(Collectors.toList()));
        template3.setHeader("header3", "value3");
        template3.setBody("body3");
        templateMap.put(templateName3, template3);

        TemplateEnricher templateEnricher = new TemplateEnricher();
        templateEnricher.setPreferences(context);

        assertTrue(templateEnricher.addTemplates(templateMap));
        Iterable<Map.Entry<String, InstallationTemplate>> expectedTemplateSet = templateMap.entrySet();
        assertEquals(expectedTemplateSet, templateEnricher.getTemplates());
        templateEnricher.removeTemplates(Stream.of(templateName, templateName2).collect(Collectors.toList()));
        assertEquals(templateMap.size() - 2,
                ((Set<Map.Entry<String, InstallationTemplate>>)templateEnricher.getTemplates()).size());
        assertNull(templateEnricher.getTemplate(templateName));
        assertNull(templateEnricher.getTemplate(templateName2));
        assertNotNull(templateEnricher.getTemplate(templateName3));
    }

    @Test
    public void templateEnricherClearTemplates () {
        TemplateEnricher templateEnricher = new TemplateEnricher();
        templateEnricher.setPreferences(context);

        assertTrue(templateEnricher.addTemplates(templateMap));
        Iterable<Map.Entry<String, InstallationTemplate>> expectedTemplateSet = templateMap.entrySet();
        assertEquals(expectedTemplateSet, templateEnricher.getTemplates());
        templateEnricher.clearTemplates();
        assertTrue(((Set<Map.Entry<String, InstallationTemplate>>) templateEnricher.getTemplates()).isEmpty());
    }
}
