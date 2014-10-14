package de.subcentral.core.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.media.SingleMedia;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.standardizing.Standardizings;
import de.subcentral.core.util.SimplePropDescriptor;

public class ReleaseParser extends AbstractMappingParser<Release>
{
	private List<ConditionalMapper<Media>>	mediaMappers	= new ArrayList<>(2);
	private Mapper<Release>					releaseMapper	= Parsings.getDefaultReleaseMapper();

	public ReleaseParser(String domain)
	{
		super(domain);
		mediaMappers.add(new ConditionalMapper<Media>(props -> props.containsKey(Series.PROP_NAME), Parsings.getDefaultEpisodeMapper()));
		mediaMappers.add(new ConditionalMapper<Media>(props -> props.containsKey(SingleMedia.PROP_NAME), Parsings.getDefaultStandardMediaMapper()));
	}

	@Override
	protected Release map(Map<SimplePropDescriptor, String> props)
	{
		// Media
		Media media = Parsings.tryMap(props, propParsingService, mediaMappers);
		Standardizings.mayStandardize(media, standardizingService);

		// Release
		Release rls = releaseMapper.map(props, propParsingService);
		rls.getMedia().add(media);
		Standardizings.mayStandardize(rls, standardizingService);
		return rls;
	}
}
