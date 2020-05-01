package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;
import androidx.test.filters.SmallTest;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SmallTest
public class TagVisitorTest {
    private Context context = getInstrumentation().getTargetContext();
    private List<String> tagList = new ArrayList<String>();

    @Before
    public void Before(){
        tagList.clear();
        tagList.add("tag1");
        tagList.add("tag2");
        tagList.add("tag3");
    }

    @Test
    public void TagEnricherAddTag () {
        TagVisitor te = new TagVisitor(context);

        assertTrue(te.addTag(tagList.get(0)));
        assertTrue(((HashSet<String>)te.getTags()).contains(tagList.get(0)));
    }

    @Test
    public void TagEnricherAddTags () {
        TagVisitor te = new TagVisitor(context);

        List<String> secondTagList = new ArrayList<String>();
        secondTagList.add("tag4");
        secondTagList.add("tag5");
        secondTagList.add("tag6");

        assertTrue(te.addTags(tagList));
        assertTrue(te.addTags(secondTagList));
        assertTrue(((HashSet<String>)te.getTags()).containsAll(tagList));
        assertTrue(((HashSet<String>)te.getTags()).containsAll(secondTagList));
        assertEquals(tagList.size() + secondTagList.size(), ((HashSet<String>)te.getTags()).size());
    }

    @Test
    public void TagEnricherClearTags () {
        TagVisitor te = new TagVisitor(context);

        assertTrue(te.addTags(tagList));
        assertTrue(((HashSet<String>)te.getTags()).containsAll(tagList));
        te.clearTags();
        assertTrue(((HashSet<String>)te.getTags()).isEmpty());
    }

    @Test
    public void TagEnricherRemoveTag () {
        TagVisitor te = new TagVisitor(context);

        assertTrue(te.addTags(tagList));
        assertTrue(((HashSet<String>)te.getTags()).containsAll(tagList));
        te.removeTag(tagList.get(0));
        assertEquals(tagList.size() - 1, ((HashSet<String>)te.getTags()).size());
        assertTrue(((HashSet<String>)te.getTags()).containsAll(tagList.subList(1, tagList.size())));
    }

    @Test
    public void TagEnricherRemoveTags () {
        TagVisitor te = new TagVisitor(context);

        assertTrue(te.addTags(tagList));
        assertTrue(((HashSet<String>)te.getTags()).containsAll(tagList));
        te.removeTags(tagList.subList(0, tagList.size() - 1));
        assertEquals(tagList.size() - 2, ((HashSet<String>)te.getTags()).size());
        assertTrue(((HashSet<String>)te.getTags()).containsAll(tagList.subList(2, tagList.size())));
    }
}
