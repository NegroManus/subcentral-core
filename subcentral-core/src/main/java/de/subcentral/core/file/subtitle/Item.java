package de.subcentral.core.file.subtitle;

import java.time.Duration;

import com.google.common.base.MoreObjects;

public class Item {
    private long   start;
    private long   end;
    private String text;

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        if (start < 0) {
            throw new IllegalArgumentException("start cannot be negative");
        }
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        if (start < 0) {
            throw new IllegalArgumentException("end cannot be negative");
        }
        this.end = end;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    // Convenience
    public long getDuration() {
        return end - start;
    }

    public void setDuration(long duration) {
        this.end = start + duration;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Item.class).omitNullValues().add("start", Duration.ofMillis(start)).add("end", Duration.ofMillis(end)).add("text", text.replace('\n', '|')).toString();
    }
}
