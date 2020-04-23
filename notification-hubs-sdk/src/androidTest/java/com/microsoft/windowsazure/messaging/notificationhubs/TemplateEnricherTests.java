package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import org.junit.Test;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TemplateEnricherTests {
    private Context context = getInstrumentation().getTargetContext();
    InstallationTemplate template1 = new InstallationTemplate();
    private List<InstallationTemplate> templateList;

    public TemplateEnricherTests() {
        template1.addTags(Stream.of("tag1", "tag2", "tag3").collect(Collectors.toList()));
        template1.setHeader("header1", "value1");
        template1.setBody("body1");
        templateList = Stream.of(template1).collect(Collectors.toList());
    }

    @Test
    public void templateEnricherAddTemplate() {
        TemplateEnricher te = new TemplateEnricher();
        te.setPreferences(context);

        assertTrue(te.addTemplate(templateList.get(0)));
        assertTrue(((HashSet<InstallationTemplate>)te.getTemplates()).contains(templateList.get(0)));
    }

    @Test
    public void templateEnricherAddTemplates () {
        InstallationTemplate template2 = new InstallationTemplate();
        template2.addTags(Stream.of("tag4", "tag5", "tag6").collect(Collectors.toList()));
        template2.setHeader("header2", "value2");
        template2.setBody("body2");
        templateList.add(template2);

        TemplateEnricher te = new TemplateEnricher();
        te.setPreferences(context);

        assertTrue(te.addTemplates(templateList));
        assertTrue(((HashSet<InstallationTemplate>) te.getTemplates()).containsAll(templateList));
    }

    @Test
    public void templateEnricherRemoveTemplate () {
        TemplateEnricher te = new TemplateEnricher();
        te.setPreferences(context);

        assertTrue(te.addTemplates(templateList));
        assertTrue(((HashSet<InstallationTemplate>)te.getTemplates()).containsAll(templateList));
        te.removeTemplate(templateList.get(0));
        assertEquals(templateList.size() - 1, ((HashSet<InstallationTemplate>)te.getTemplates()).size());
        assertTrue(((HashSet<InstallationTemplate>)te.getTemplates()).containsAll(templateList.subList(1, templateList.size())));
    }

    @Test
    public void templateEnricherRemoveTemplates () {
        InstallationTemplate template2 = new InstallationTemplate();
        template2.addTags(Stream.of("tag4", "tag5", "tag6").collect(Collectors.toList()));
        template2.setHeader("header2", "value2");
        template2.setBody("body2");
        templateList.add(template2);

        InstallationTemplate template3 = new InstallationTemplate();
        template3.addTags(Stream.of("tag7", "tag8", "tag9").collect(Collectors.toList()));
        template3.setHeader("header3", "value3");
        template3.setBody("body3");
        templateList.add(template3);

        TemplateEnricher te = new TemplateEnricher();
        te.setPreferences(context);

        assertTrue(te.addTemplates(templateList));
        assertTrue(((HashSet<InstallationTemplate>)te.getTemplates()).containsAll(templateList));
        te.removeTemplates(templateList.subList(0, templateList.size() - 1));
        assertEquals(templateList.size() - 2, ((HashSet<InstallationTemplate>)te.getTemplates()).size());
        assertTrue(((HashSet<InstallationTemplate>)te.getTemplates()).containsAll(templateList.subList(2, templateList.size())));
    }

    @Test
    public void templateEnricherClearTaemplates () {
        TemplateEnricher te = new TemplateEnricher();
        te.setPreferences(context);

        assertTrue(te.addTemplates(templateList));
        assertTrue(((HashSet<InstallationTemplate>) te.getTemplates()).contains(template1));
        te.clearTemplates();
        assertTrue(((HashSet<InstallationTemplate>)te.getTemplates()).isEmpty());
    }
}
