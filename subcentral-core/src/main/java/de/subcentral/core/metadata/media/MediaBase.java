package de.subcentral.core.metadata.media;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.metadata.MetadataBase;
import de.subcentral.core.metadata.Site;
import de.subcentral.core.util.ValidationUtil;

public abstract class MediaBase extends MetadataBase implements Media {
    private static final long                    serialVersionUID = 1648358057912659544L;

    protected String                             title;
    protected Temporal                           date;
    protected String                             description;
    protected Map<Site, Float>                   ratings          = new HashMap<>(2);
    protected String                             contentRating;
    protected final ListMultimap<String, String> images           = LinkedListMultimap.create(0);
    protected final List<String>                 furtherInfoLinks = new ArrayList<>(4);

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Temporal getDate() {
        return date;
    }

    public void setDate(Temporal date) throws IllegalArgumentException {
        this.date = ValidationUtil.validateTemporalClass(date);
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Map<Site, Float> getRatings() {
        return ratings;
    }

    public void setRatings(Map<Site, Float> ratings) {
        this.ratings.clear();
        this.ratings.putAll(ratings);
    }

    @Override
    public String getContentRating() {
        return contentRating;
    }

    public void setContentRating(String contentRating) {
        this.contentRating = contentRating;
    }

    @Override
    public ListMultimap<String, String> getImages() {
        return images;
    }

    public void setImages(ListMultimap<String, String> images) {
        this.images.clear();
        this.images.putAll(images);
    }

    @Override
    public List<String> getFurtherInfoLinks() {
        return furtherInfoLinks;
    }

    public void setFurtherInfoLinks(Collection<String> furtherInfoLinks) {
        this.furtherInfoLinks.clear();
        this.furtherInfoLinks.addAll(furtherInfoLinks);
    }
}
