package de.subcentral.core.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.media.SingleMedia;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.util.SimplePropDescriptor;

public class ReleaseParser extends AbstractMappingParser<Release>
{
	private List<ConditionalMapper<List<? extends Media>>>	mediaMappers	= new ArrayList<>(3);
	private Mapper<Release>									releaseMapper	= Parsings.getDefaultReleaseMapper();

	public ReleaseParser(String domain)
	{
		super(domain);
		mediaMappers.add(new ConditionalMapper<List<? extends Media>>(MultiEpisodeMapper::containsMultiEpisode,
				Parsings.getDefaultMultiEpisodeMapper()));
		mediaMappers.add(new ConditionalMapper<List<? extends Media>>(props -> props.containsKey(Series.PROP_NAME),
				Parsings.createSingletonListMapper(Parsings.getDefaultEpisodeMapper())));
		mediaMappers.add(new ConditionalMapper<List<? extends Media>>(props -> props.containsKey(SingleMedia.PROP_NAME),
				Parsings.createSingletonListMapper(Parsings.getDefaultSingleMediaMapper())));
	}

	@Override
	protected Release map(Map<SimplePropDescriptor, String> props)
	{
		// Media
		List<? extends Media> media = Parsings.tryMap(props, propParsingService, mediaMappers);

		// Release
		Release rls = releaseMapper.map(props, propParsingService);
		rls.getMedia().addAll(media);
		return rls;
	}
}
