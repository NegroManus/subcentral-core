package de.subcentral.core.correct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Test;

import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.Tags;

public class TagsReplacerTest {
    private final List<Tag>    input              = Tags.of("WEB-DL", "DD5", "1", "H", "264");
    private final TagsReplacer noOpReplacer       = new TagsReplacer(Tags.of("DD5", "2"), Tags.of("DD5.2"));
    private final TagsReplacer dd51Replacer       = new TagsReplacer(Tags.of("DD5", "1"), Tags.of("DD5.1"));
    private final TagsReplacer h264Replacer       = new TagsReplacer(Tags.of("H", "264"), Tags.of("H.264"));
    private final TagsReplacer splitWebDlReplacer = new TagsReplacer(Tags.of("WEB-DL"), Tags.of("WEB", "DL"));

    @Test
    public void testReplaceSequenceNoOp() {
        List<Tag> result = noOpReplacer.apply(input);
        assertSame(input, result);
    }

    @Test
    public void testReplaceSequenceDD51() {
        List<Tag> result = dd51Replacer.apply(input);
        assertEquals(Tags.of("WEB-DL", "DD5.1", "H", "264"), result);
        assertNotSame(input, result);
    }

    @Test
    public void testReplaceSequenceH264() {
        List<Tag> result = h264Replacer.apply(input);
        assertEquals(Tags.of("WEB-DL", "DD5", "1", "H.264"), result);
        assertNotSame(input, result);
    }

    @Test
    public void testReplaceSequenceDd51AndH264() {
        List<Tag> result = dd51Replacer.andThen(h264Replacer).apply(input);
        assertEquals(Tags.of("WEB-DL", "DD5.1", "H.264"), result);
        assertNotSame(input, result);
    }

    @Test
    public void testReplaceSequenceH264AndDd51() {
        List<Tag> result = h264Replacer.andThen(dd51Replacer).apply(input);
        assertEquals(Tags.of("WEB-DL", "DD5.1", "H.264"), result);
        assertNotSame(input, result);
    }

    @Test
    public void testReplaceSequenceSplitWebDl() {
        List<Tag> result = splitWebDlReplacer.apply(input);
        assertEquals(Tags.of("WEB", "DL", "DD5", "1", "H", "264"), result);
        assertNotSame(input, result);
    }
}
