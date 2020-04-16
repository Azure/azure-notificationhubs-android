package com.microsoft.windowsazure.messaging;

import android.content.Context;
import androidx.test.filters.SmallTest;
import com.microsoft.windowsazure.messaging.notificationhubs.TagEnricher;
import org.junit.Test;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SmallTest
public class TagEnricherTest {
    private Context context = getInstrumentation().getTargetContext();
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

        List<String> secondTagList = Stream.of("tag4", "tag5", "tag6").collect(Collectors.toList());

        assertTrue(te.addTags(tagList));
        assertTrue(te.addTags(secondTagList));
        assertTrue(((HashSet<String>)te.getTags()).containsAll(tagList));
        assertTrue(((HashSet<String>)te.getTags()).containsAll(secondTagList));
        assertEquals(tagList.size() + secondTagList.size(), ((HashSet<String>)te.getTags()).size());
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
