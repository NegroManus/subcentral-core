package de.subcentral.mig.process;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Year;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.List;

import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.mig.Migration;
import de.subcentral.support.subcentralde.SubCentralDe;

public class SubCentralSubManDbApi extends SubCentralDbApi
{
	public void clearData() throws SQLException
	{
		try (Statement stmt = connection.createStatement())
		{
			stmt.addBatch("DELETE FROM sc1_series");
			int[] results = stmt.executeBatch();
		}
	}

	public void insertSeriesFromSeriesList(Series series) throws SQLException
	{
		if (series.getIds().containsKey(SubCentralDe.SOURCE_ID))
		{
			// already inserted
			return;
		}

		// May insert network
		// TODO

		// May insert genres
		//
		try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO sc1_series (name, boardID, type, start, end, logo) VALUES (?, ?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS))
		{
			stmt.setString(1, series.getName());
			setInteger(stmt, 2, series.getAttributeValue(Migration.SERIES_ATTR_BOARD_ID));
			stmt.setString(3, Series.TYPE_SEASONED);
			Date start = convertYearToDate(series.getDate());
			stmt.setDate(4, start);
			Date end = convertYearToDate(series.getFinaleDate());
			stmt.setDate(5, end);
			String logo = getFirstImage(series, Migration.IMG_TYPE_SERIES_LOGO);
			stmt.setString(6, logo);

			int affectedRows = stmt.executeUpdate();
			checkUpdated(series, affectedRows);
			int id = getGeneratedId(stmt);
			series.getIds().put(SubCentralDe.SOURCE_ID, id + "");
		}
	}

	private void setInteger(PreparedStatement stmt, int index, Integer value) throws SQLException
	{
		if (value != null)
		{
			stmt.setInt(index, value.intValue());
		}
		else
		{
			stmt.setNull(index, Types.INTEGER);
		}
	}

	private Date convertYearToDate(Temporal temporal)
	{
		if (temporal == null)
		{
			return null;
		}
		Year year = Year.of(temporal.get(ChronoField.YEAR));
		return new Date(year.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
	}

	private String getFirstImage(Media media, String imageType)
	{
		List<String> imgs = media.getImages().get(imageType);
		if (imgs.isEmpty())
		{
			return null;
		}
		return imgs.get(0);
	}

	private int getGeneratedId(Statement stmt) throws SQLException
	{
		try (ResultSet generatedKeys = stmt.getGeneratedKeys())
		{
			if (generatedKeys.next())
			{
				int id = generatedKeys.getInt(1);
				return id;
			}
			else
			{
				throw new SQLException("UPDATE failed: No ID obtained");
			}
		}
	}
}
