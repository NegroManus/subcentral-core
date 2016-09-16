package de.subcentral.mig.preview;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
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
import de.subcentral.mig.parse.SeriesListParser.SeriesListData;
import de.subcentral.mig.process.MigrationAssistance;
import de.subcentral.mig.process.MigrationService;

@WebServlet("/overview")
public class OverviewServlet extends HttpServlet {
	private static final long	serialVersionUID	= -468235743950296120L;
	private static final Logger	log					= LogManager.getLogger(OverviewServlet.class);

	private MigrationAssistance	assistance;

	@Override
	public void init() throws ServletException {
		try {
			// Do required initialization
			assistance = new MigrationAssistance();
			assistance.setEnvironmentSettingsFile(Paths.get(MigrationPreview.ENV_SETTINGS_PATH));
			assistance.loadEnvironmentSettingsFromFile();
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Set response content type
		response.setContentType("text/html");

		// Actual logic goes here.
		try {
			SeriesListData data = readSeriesListData();
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
			for (Series series : data.getSeries()) {
				for (Season season : series.getSeasons()) {
					writer.print("<option value=\"");
					Integer seasonThreadId = season.getFirstAttributeValue(Migration.SEASON_ATTR_THREAD_ID);
					if (seasonThreadId == null) {
						seasonThreadId = -1;
					}
					writer.print(seasonThreadId.toString());
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
		catch (Exception e) {
			throw new ServletException(e);
		}
	}

	public SeriesListData readSeriesListData() throws SQLException {
		try (MigrationService service = assistance.createMigrationService()) {
			return service.readSeriesList();
		}
	}

	@Override
	public void destroy() {

	}
}