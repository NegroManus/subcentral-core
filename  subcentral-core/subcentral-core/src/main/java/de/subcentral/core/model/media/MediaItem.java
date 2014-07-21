package de.subcentral.core.model.media;

/**
 * Marker interface for Media items. Media items are - in contrast to MediaCollections - atomic media. For example an Episode or a Song. MediaItems
 * can be collected in MediaCollections, such as a Season / Series or an Album.
 *
 * Subtitles can only be created for MediaItems. Not for MediaCollections.
 */
public interface MediaItem extends Media
{

}
