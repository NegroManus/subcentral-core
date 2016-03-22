package de.subcentral.mig.preview;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.ContributionUtil;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.name.EpisodeNamer;
import de.subcentral.core.name.NamingDefaults;
import de.subcentral.core.name.NamingService;
import de.subcentral.core.name.SeasonNamer;
import de.subcentral.mig.Migration;
import de.subcentral.mig.MigrationConfig;
import de.subcentral.mig.process.SeasonPostParser;
import de.subcentral.mig.process.SeasonPostParser.SeasonPostData;
import de.subcentral.support.woltlab.WoltlabBurningBoard;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbPost;

@WebServlet("/season")
public class SeasonServlet extends HttpServlet
{
	private static final long	serialVersionUID	= -3990408720731314570L;
	private static final Logger	log					= LogManager.getLogger(SeasonServlet.class);

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
			int seasonThreadId = Integer.parseInt(request.getParameter("threadId"));
			SeasonPostData data = readSeasonPostData(seasonThreadId);

			PrintWriter writer = response.getWriter();
			writer.println("<html>");
			writer.println("<head>");
			writer.println("<title>SubCentral Migration Preview</title>");
			writer.println("</head>");
			writer.println("<body>");
			writer.println("<a href=\"/overview\">Zurück zur Übersicht</a>");

			writer.println("<p>");
			writer.println("<div><h2>Serie</h2>");
			writer.println("<h3>" + name(data.getSeries()) + "</h3>");
			writer.println(data.getSeries());
			writer.println("</div>");

			SortedSet<Subtitle> subs = new TreeSet<>();
			for (SubtitleRelease subRls : data.getSubtitleFiles())
			{
				subs.addAll(subRls.getSubtitles());
			}

			writer.println("<div><h2>Staffeln</h2>");
			for (Season season : data.getSeasons())
			{
				writer.println("<div><h3>" + name(season) + "</h3>");
				writer.println(season);
				writer.println("<div><h2>Episoden</h2>");
				for (Episode epi : season.getEpisodes())
				{
					writer.println("<h3>" + name(epi) + "</h3>");
					writer.println(epi);
					writer.println("<br/><br/>");

					for (Subtitle sub : subs)
					{
						if (epi.equals(sub.getMedia()))
						{
							writer.println(languageToHtml(sub.getLanguage()) + " " + sub.getSource());
							writer.print(" ");
							writer.print(printContributionsByType(sub.getContributions()));

							for (SubtitleRelease subRls : data.getSubtitleFiles())
							{
								if (subRls.getSubtitles().contains(sub))
								{
									writer.print("</br> - " + subRls.getMatchingReleases().stream().map((Release r) -> r.getName()).collect(Collectors.joining(", ")));
									writer.print(" ");
									writer.print(printContributionsByType(subRls.getContributions()));
									writer.println(" [" + subRls.getAttributeValue(Migration.SUBTITLE_FILE_ATTR_ATTACHMENT_ID) + "]");
								}
							}

							writer.println("<hr>");
						}
					}
				}
				writer.println("</div>");
				writer.println("</div>");
			}
			writer.println("</p>");

			writer.println("</body>");
			writer.println("</html>");
			writer.flush();
		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}
	}

	private static String languageToHtml(String language)
	{
		switch (language)
		{
			case Migration.SUBTITLE_LANGUAGE_GERMAN:
				return "<img src=\"img/de.png\" alt=\"" + language + "\" title=\"" + language + "\" />";
			case Migration.SUBTITLE_LANGUAGE_ENGLISH:
				return "<img src=\"img/usa.png\" alt=\"" + language + "\" title=\"" + language + "\" />";
			default:
				return "[" + language + "]";
		}
	}

	private static String printContributionsByType(Collection<Contribution> contributions)
	{
		if (contributions.isEmpty())
		{
			return "";
		}
		Set<Map.Entry<String, Collection<Contribution>>> contributionsByType = ContributionUtil.groupByType(contributions).asMap().entrySet();
		return "(" + contributionsByType.stream().map(SeasonServlet::printContributionTypeContributions).collect(Collectors.joining("; ")) + ")";
	}

	private static String printContributionTypeContributions(Map.Entry<String, Collection<Contribution>> entry)
	{
		return entry.getKey() + ": " + entry.getValue().stream().map(SeasonServlet::printContributorName).collect(Collectors.joining(", "));
	}

	private static String printContributorName(Contribution c)
	{
		return c.getContributor().getName();
	}

	private String name(Object obj)
	{
		NamingService ns = NamingDefaults.getDefaultNamingService();
		Map<String, Object> np = ImmutableMap.of(SeasonNamer.PARAM_ALWAYS_INCLUDE_TITLE, Boolean.TRUE, EpisodeNamer.PARAM_ALWAYS_INCLUDE_TITLE, Boolean.TRUE);
		return ns.name(obj, np);
	}

	private SeasonPostData readSeasonPostData(int seasonThreadId) throws SQLException
	{
		WbbPost post;
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard scBoard = new WoltlabBurningBoard();
			scBoard.setConnection(conn);
			post = scBoard.getFirstPost(seasonThreadId);
		}

		return new SeasonPostParser().parse(post.getTopic(), post.getMessage());
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