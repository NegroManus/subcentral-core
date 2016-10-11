package de.subcentral.core.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.Tags;

public class CollectionUtilTest {
    @Test
    public void testUpdateSortedListNaturalOrder() {
        // Input
        List<Integer> origList = CollectionUtil.createArrayList(1, 2, 3, 4, 6);
        List<Integer> updateList = ImmutableList.of(1, 2, 3, 4, 5, 6);
        // Apply
        CollectionUtil.updateSortedList(origList, updateList);
        // Assert
        assertEquals(updateList, origList);
    }

    @Test
    public void testTransferMetaTags() {
        List<Tag> sourceTags = Tags.of("PROPER", "720p", "HDTV", "x264");
        List<Tag> targetTags = Tags.of("HDTV", "x264");
        List<Tag> metaTags = Tags.of("REAL", "PROPER", "REPACK", "DiRFIX", "NFOFIX", "iNTERNAL");
        CollectionUtil.transferElementsToHead(sourceTags, targetTags, metaTags);
        assertEquals(Tags.of("PROPER", "HDTV", "x264"), targetTags);

        sourceTags = Tags.of("REAL", "PROPER", "720p", "HDTV", "x264");
        targetTags = Tags.of("HDTV", "x264");
        metaTags = Tags.of("REAL", "PROPER", "REPACK", "DiRFIX", "NFOFIX", "iNTERNAL");
        CollectionUtil.transferElementsToHead(sourceTags, targetTags, metaTags);
        assertEquals(Tags.of("REAL", "PROPER", "HDTV", "x264"), targetTags);
    }
}
