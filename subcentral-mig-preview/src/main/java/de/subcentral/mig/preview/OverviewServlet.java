package de.subcentral.mig.preview;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.name.NamingDefaults;
import de.subcentral.mig.Migration;
import de.subcentral.mig.MigrationConfig;
import de.subcentral.mig.parse.SeriesListParser;
import de.subcentral.mig.parse.SeriesListParser.SeriesListData;
import de.subcentral.support.woltlab.WoltlabBurningBoard;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbPost;

@WebServlet("/overview")
public class OverviewServlet extends HttpServlet
{
	private static final long	serialVersionUID	= -468235743950296120L;
	private static final Logger	log					= LogManager.getLogger(OverviewServlet.class);

	private static final String	ENV_SETTINGS_PATH	= "C:\\Users\\mhertram\\Documents\\Projekte\\SC\\Submanager\\mig\\migration-env-settings.properties";
	private MigrationConfig		config;

	@Override
	public void init() throws ServletException
	{
		try
		{
			// Do required initialization
			config = new MigrationConfig();
			config.setEnvironmentSettingsFile(Paths.get(ENV_SETTINGS_PATH));
			config.loadEnvironmentSettings();
			config.createDateSource();
		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		// Set response content type
		response.setContentType("text/html");

		// Actual logic goes here.
		try
		{
			SeriesListData data = readSeriesListContent();
			PrintWriter writer = response.getWriter();

			writer.println("<html>");
			writer.println("<head>");
			writer.println("<title>SubCentral Migration Preview</title>");
			writer.println("</head>");
			writer.println("<body>");

			writer.println("In der Serienliste sind gelistet: ");
			writer.println("<ul>");
			writer.println("<li>" + data.getSeries().size() + " Serien</li>");
			writer.println("<li>" + data.getSeasons().size() + " Staffeln</li>");
			writer.println("</ul>");

			writer.println("<form id=\"seasonselectform\" action=\"season\">");
			writer.println("<p><select name=\"threadId\" onchange=\"this.form.submit();\">");
			writer.print("<option value=\"\">Staffel wählen</option>");
			for (Series series : data.getSeries())
			{
				for (Season season : series.getSeasons())
				{
					writer.print("<option value=\"");
					writer.print(Integer.toString(season.getAttributeValue(Migration.SEASON_ATTR_THREAD_ID)));
					writer.print("\">");
					writer.print(NamingDefaults.getDefaultSeasonNamer().name(season));
					writer.println("</option>");
				}
			}
			writer.println("</select></p>");
			writer.println("</form>");

			writer.println("</body>");
			writer.println("</html>");
			writer.flush();
		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}
	}

	private SeriesListData readSeriesListContent() throws SQLException
	{
		log.debug("Reading SeriesList content");
		int seriesListPostId = config.getEnvironmentSettings().getInt("sc.serieslist.postid");
		SeriesListData seriesListContent;
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard scBoard = new WoltlabBurningBoard();
			scBoard.setConnection(conn);
			WbbPost seriesListPost = scBoard.getPost(seriesListPostId);
			seriesListContent = new SeriesListParser().parsePost(seriesListPost.getMessage());
			log.debug("Read SeriesList content: {} series, {} seasons, {} networks",
					seriesListContent.getSeries().size(),
					seriesListContent.getSeasons().size(),
					seriesListContent.getNetworks().size());
			return seriesListContent;
		}
	}

	@Override
	public void destroy()
	{
		try
		{
			if (config != null)
			{
				config.closeDataSource();
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}