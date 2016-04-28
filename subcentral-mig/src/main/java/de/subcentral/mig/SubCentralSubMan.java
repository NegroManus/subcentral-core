package de.subcentral.mig;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Year;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.media.Network;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.support.woltlab.AbstractSqlApi;

public class SubCentralSubMan extends AbstractSqlApi
{
	private static final Logger log = LogManager.getLogger(SubCentralSubMan.class);

	public SubCentralSubMan(Connection connection)
	{
		super(connection);
	}

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
		String id = series.getId(SubCentralDe.getSite());
		if (id != null)
		{
			log.debug("The series {} already has the id {}. It will not be inserted", series, id);
			return;
		}

		try
		{
			connection.setAutoCommit(false);

			// Get network Id
			String networkId = null;
			if (!series.getNetworks().isEmpty())
			{
				Network network = series.getNetworks().get(0);
				networkId = network.getId(SubCentralDe.getSite());
				if (networkId == null)
				{

				}
			}

			// May insert genres
			//
			try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO sc1_series (name, boardID, type, start, end, networkID, logo) VALUES (?, ?, ?, ?, ?, ?);",
					Statement.RETURN_GENERATED_KEYS))
			{
				stmt.setString(1, series.getName());
				setInteger(stmt, 2, series.getFirstAttributeValue(Migration.SERIES_ATTR_BOARD_ID));
				stmt.setString(3, Series.TYPE_SEASONED);
				Date start = convertYearToDate(series.getDate());
				stmt.setDate(4, start);
				Date end = convertYearToDate(series.getFinaleDate());
				stmt.setDate(5, end);
				stmt.setString(6, networkId);
				String logo = getFirstImage(series, Migration.SERIES_IMG_TYPE_LOGO);
				stmt.setString(7, logo);

				int affectedRows = stmt.executeUpdate();
				checkUpdated(series, affectedRows);
				int generatedId = getGeneratedId(stmt);
				series.setId(SubCentralDe.getSite(), Integer.toString(generatedId));
			}
		}
		catch (SQLException e)
		{
			log.error("Exception while inserting series. Rolling back ", e);
			connection.rollback();
			throw e;
		}
		finally
		{
			connection.setAutoCommit(true);
		}
	}

	private static void setInteger(PreparedStatement stmt, int index, Integer value) throws SQLException
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

	private static Date convertYearToDate(Temporal temporal)
	{
		if (temporal == null)
		{
			return null;
		}
		Year year = Year.from(temporal);
		return new Date(year.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
	}

	private static String getFirstImage(Media media, String imageType)
	{
		List<String> imgs = media.getImages().get(imageType);
		if (imgs.isEmpty())
		{
			return null;
		}
		return imgs.get(0);
	}

	private static int getGeneratedId(Statement stmt) throws SQLException
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
