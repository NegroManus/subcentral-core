package de.subcentral.mig;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.support.subcentralde.SubCentralDe;

public class SubCentralSubManDbApiTest
{

	@Test
	public void testInsertSeries() throws SQLException, ConfigurationException
	{
		Series series = new Series("Psych");
		series.setType(Series.TYPE_SEASONED);

		try (Connection conn = MigTestUtil.connect())
		{
			SubCentralSubMan api = new SubCentralSubMan(conn);
			api.insertSeriesFromSeriesList(series);

			System.out.println(series.getIds().get(SubCentralDe.SITE));

			api.clearData();
		}

	}
}
