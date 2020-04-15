package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class TagEnricherTest {
    private Context context = ApplicationProvider.getApplicationContext();
    private List<String> tagList = Stream.of("tag1", "tag2", "tag3").collect(Collectors.toList());

    @Test
    public void TagEnricherAddTag () {
        TagEnricher te = new TagEnricher();
        te.SetPreferences(context);

        assertTrue(te.addTag(tagList.get(0)));
        assertTrue(((HashSet<String>)te.getTags()).contains(tagList.get(0)));
    }

    @Test
    public void TagEnricherAddTags () {
        TagEnricher te = new TagEnricher();
        te.SetPreferences(context);

        assertTrue(te.addTags(tagList));
        assertTrue(((HashSet<String>)te.getTags()).containsAll(tagList));
        assertEquals(((HashSet<String>)te.getTags()).size(), tagList.size());
    }

    @Test
    public void TagEnricherClearTags () {
        TagEnricher te = new TagEnricher();
        te.SetPreferences(context);

        assertTrue(te.addTags(tagList));
        assertTrue(((HashSet<String>)te.getTags()).containsAll(tagList));
        te.clearTags();
        assertTrue(((HashSet<String>)te.getTags()).isEmpty());
    }

    @Test
    public void TagEnricherRemoveTag () {
        TagEnricher te = new TagEnricher();
        te.SetPreferences(context);

        assertTrue(te.addTags(tagList));
        assertTrue(((HashSet<String>)te.getTags()).containsAll(tagList));
        te.removeTag(tagList.get(0));
        assertEquals(tagList.size() - 1, ((HashSet<String>)te.getTags()).size());
        assertTrue(((HashSet<String>)te.getTags()).containsAll(tagList.subList(1, tagList.size())));
    }

    @Test
    public void TagEnricherRemoveTags () {
        TagEnricher te = new TagEnricher();
        te.SetPreferences(context);

        assertTrue(te.addTags(tagList));
        assertTrue(((HashSet<String>)te.getTags()).containsAll(tagList));
        te.removeTags(tagList.subList(0, tagList.size() - 1));
        assertEquals(tagList.size() - 2, ((HashSet<String>)te.getTags()).size());
        assertTrue(((HashSet<String>)te.getTags()).containsAll(tagList.subList(2, tagList.size())));
    }
}
