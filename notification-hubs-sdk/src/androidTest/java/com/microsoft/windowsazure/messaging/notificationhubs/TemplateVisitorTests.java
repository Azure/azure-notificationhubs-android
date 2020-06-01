package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TemplateVisitorTests {
    private Context context = getInstrumentation().getTargetContext();
    String templateName;
    InstallationTemplate template = new InstallationTemplate();
    private Map<String, InstallationTemplate> templateMap;

    public TemplateVisitorTests() {
        templateName = "templateName";

        template.addTags(new ArrayList<String>(){{ add("tag1"); add("tag2"); add("tag3"); }});
        template.setHeader("header1", "value1");
        template.setBody("body1");
        templateMap = new HashMap<String, InstallationTemplate>(){{ put(templateName, template); }};
    }

    @Test
    public void templateVisitorSetTemplateAdd() {
        TemplateVisitor templateVisitor = new TemplateVisitor(context);

        templateVisitor.setTemplate(templateName, template);
        Iterable<Map.Entry<String, InstallationTemplate>> expectedTemplateSet = templateMap.entrySet();
        assertEquals(expectedTemplateSet, templateVisitor.getTemplates());
        assertEquals(template, templateVisitor.getTemplate(templateName));
    }

    @Test
    public void templateVisitorSetTemplateUpdate() {
        TemplateVisitor templateVisitor = new TemplateVisitor(context);

        templateVisitor.setTemplate(templateName, template);
        InstallationTemplate updatedTemplate =  templateVisitor.getTemplate(templateName);
        updatedTemplate.addTag("tag_updated");
        updatedTemplate.removeTag("tag1");
        updatedTemplate.setHeader("header1", "value_updated");
        updatedTemplate.setHeader("header2", "value2");
        updatedTemplate.setBody("body_updated");

        templateVisitor.setTemplate(templateName, updatedTemplate);
        assertEquals(updatedTemplate, templateVisitor.getTemplate(templateName));
    }

    @Test
    public void templateVisitorSetTemplates () {
        String templateName2 = "templateName2";
        InstallationTemplate template2 = new InstallationTemplate();
        template2.addTags(new ArrayList<String>(){{ add("tag4"); add("tag5"); add("tag6"); }});
        template2.setHeader("header2", "value2");
        template2.setBody("body2");
        templateMap.put(templateName2, template2);

        TemplateVisitor templateVisitor = new TemplateVisitor(context);

        templateVisitor.setTemplates(templateMap);
        Iterable<Map.Entry<String, InstallationTemplate>> expectedTemplateSet = templateMap.entrySet();
        assertEquals(expectedTemplateSet, templateVisitor.getTemplates());
        assertEquals(template, templateVisitor.getTemplate(templateName));
        assertEquals(template2, templateVisitor.getTemplate(templateName2));
    }

    @Test
    public void templateVisitorRemoveTemplate () {
        TemplateVisitor templateVisitor = new TemplateVisitor(context);

        templateVisitor.setTemplates(templateMap);
        Iterable<Map.Entry<String, InstallationTemplate>> expectedTemplateSet = templateMap.entrySet();
        assertEquals(expectedTemplateSet, templateVisitor.getTemplates());
        templateVisitor.removeTemplate(templateName);
        assertEquals(templateMap.size() - 1,
                ((Set<Map.Entry<String, InstallationTemplate>>) templateVisitor.getTemplates()).size());
        assertNull(templateVisitor.getTemplate(templateName));
    }

    @Test
    public void templateVisitorRemoveTemplates () {
        final String templateName2 = "templateName2";
        InstallationTemplate template2 = new InstallationTemplate();
        template2.addTags(new ArrayList<String>(){{ add("tag4"); add("tag5"); add("tag6"); }});
        template2.setHeader("header2", "value2");
        template2.setBody("body2");
        templateMap.put(templateName2, template2);

        final String templateName3 = "templateName3";
        InstallationTemplate template3 = new InstallationTemplate();
        template3.addTags(new ArrayList<String>(){{ add("tag7"); add("tag8"); add("tag9"); }});
        template3.setHeader("header3", "value3");
        template3.setBody("body3");
        templateMap.put(templateName3, template3);

        TemplateVisitor templateVisitor = new TemplateVisitor(context);

        templateVisitor.setTemplates(templateMap);
        Iterable<Map.Entry<String, InstallationTemplate>> expectedTemplateSet = templateMap.entrySet();
        assertEquals(expectedTemplateSet, templateVisitor.getTemplates());
        templateVisitor.removeTemplates(new ArrayList<String>(){{ add(templateName); add(templateName2); add("tag3"); }});
        assertEquals(templateMap.size() - 2,
                ((Set<Map.Entry<String, InstallationTemplate>>) templateVisitor.getTemplates()).size());
        assertNull(templateVisitor.getTemplate(templateName));
        assertNull(templateVisitor.getTemplate(templateName2));
        assertNotNull(templateVisitor.getTemplate(templateName3));
    }

    @Test
    public void templateVisitorClearTemplates () {
        TemplateVisitor templateVisitor = new TemplateVisitor(context);

        templateVisitor.setTemplates(templateMap);
        Iterable<Map.Entry<String, InstallationTemplate>> expectedTemplateSet = templateMap.entrySet();
        assertEquals(expectedTemplateSet, templateVisitor.getTemplates());
        templateVisitor.clearTemplates();
        assertTrue(((Set<Map.Entry<String, InstallationTemplate>>) templateVisitor.getTemplates()).isEmpty());
    }
}
