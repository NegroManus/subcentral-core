package de.subcentral.mig.preview;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.ContributionUtil;
import de.subcentral.core.metadata.Site;
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
import de.subcentral.mig.parse.SeasonPostParser.SeasonPostData;
import de.subcentral.mig.process.MigrationAssistance;
import de.subcentral.mig.process.MigrationService;

@WebServlet("/season")
public class SeasonServlet extends HttpServlet
{
	private static final long	serialVersionUID	= -3990408720731314570L;
	private static final Logger	log					= LogManager.getLogger(SeasonServlet.class);

	private static final String	ENV_SETTINGS_PATH	= "C:\\Users\\mhertram\\Documents\\Projekte\\SC\\Submanager\\mig\\migration-env-settings.properties";

	private MigrationAssistance	assistance;

	@Override
	public void init() throws ServletException
	{
		try
		{
			// Do required initialization
			assistance = new MigrationAssistance();
			assistance.setEnvironmentSettingsFile(Paths.get(MigrationPreview.ENV_SETTINGS_PATH));
			assistance.loadEnvironmentSettingsFromFile();
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
			String contextPath = request.getContextPath();

			int seasonThreadId = Integer.parseInt(request.getParameter("threadId"));

			SeasonPostData data = readSeasonPostData(seasonThreadId);
			// Keep a list of the subtitle releases that could not be matched with episodes
			List<SubtitleRelease> unmatchedSubtitleReleases = new ArrayList<>(data.getSubtitleReleases());
			// Group the subtitle releases by their subtitles
			// LinkedListMultimap maintains ordering for both keys and values
			Multimap<Subtitle, SubtitleRelease> subs = LinkedListMultimap.create();
			for (SubtitleRelease subRls : data.getSubtitleReleases())
			{
				if (subRls.getSubtitles().isEmpty())
				{
					subs.put(null, subRls);
				}
				else
				{
					for (Subtitle sub : subRls.getSubtitles())
					{
						subs.put(sub, subRls);
					}
				}
			}

			PrintWriter writer = response.getWriter();
			writer.println("<html>");
			writer.println("<head>");
			writer.println("<title>SubCentral Migration Preview</title>");
			writer.println("</head>");
			writer.println("<body>");
			writer.println("<a href=\"" + contextPath + "/overview\">Zurück zur Übersicht</a>");

			writer.println("<p>");
			writer.println("<div><h2>Serie</h2>");
			if (data.getSeries() == null)
			{
				writer.println("Keine Serie vorhanden");
			}
			else
			{
				writer.println("<h3>" + name(data.getSeries()) + "</h3>");
				writer.println("<code>");
				writer.println(data.getSeries());
				writer.println("</code>");
			}
			writer.println("</div>");

			writer.println("<div><h2>Staffeln</h2>");
			if (data.getSeasons().isEmpty())
			{
				writer.println("Keine Staffeln vorhanden");
			}
			else
			{
				for (Season season : data.getSeasons())
				{
					writer.println("<div><h3>" + name(season) + "</h3>");
					writer.println("<code>");
					writer.println(season);
					writer.println("</code>");
					writer.println("</div>");
				}
			}

			writer.println("<div><h2>Episoden</h2>");
			if (data.getEpisodes().isEmpty())
			{
				writer.println("Keine Episoden vorhanden");
			}
			else
			{
				for (Episode epi : data.getEpisodes())
				{
					writer.println("<h3>" + name(epi) + "</h3>");
					writer.println("<code>");
					writer.println(epi);
					writer.println("</code>");
					writer.println("<br/><br/>");
					printSubs(writer, subs, unmatchedSubtitleReleases, epi);
				}
			}
			writer.println("</div>");

			writer.println("<div><h2>Unzugeordnete Untertitel</h2>");
			if (unmatchedSubtitleReleases.isEmpty())
			{
				writer.println("Keine unzugeordneten Untertitel vorhanden");
			}
			else
			{
				printSubs(writer, subs, unmatchedSubtitleReleases, null);
			}
			writer.println("</div>");

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

	private static void printSubs(PrintWriter writer, Multimap<Subtitle, SubtitleRelease> subs, List<SubtitleRelease> unmatchedSubtitleReleases, Episode epi)
	{
		for (Map.Entry<Subtitle, Collection<SubtitleRelease>> entry : subs.asMap().entrySet())
		{
			Subtitle sub = entry.getKey();
			if (sub == null || epi == null || epi.equals(sub.getMedia()))
			{
				writer.println(printSubtitle(sub));

				writer.println("<ul>");
				for (SubtitleRelease subRls : entry.getValue())
				{
					// If the the subRls could be matched, remove it from the unmatched list
					unmatchedSubtitleReleases.remove(subRls);

					String attachmentId = Integer.toString((Integer) subRls.getAttributeValue(Migration.SUBTITLE_FILE_ATTR_ATTACHMENT_ID));
					writer.print("<li>");
					writer.print("<a href=\"https://www.subcentral.de/index.php?page=Attachment&attachmentID=" + attachmentId + "\">");
					writer.print(printReleases(subRls.getMatchingReleases()));
					writer.print("</a>");
					writer.print(" ");
					writer.print(printContributionsByType(subRls.getContributions()));
					writer.println("</li>");
				}
				writer.println("</ul>");
				writer.println("<hr>");
			}
		}
	}

	private static String printSubtitle(Subtitle sub)
	{
		if (sub != null)
		{
			return languageToHtml(sub.getLanguage()) + " " + printSource(sub.getSource()) + " " + printContributionsByType(sub.getContributions());
		}
		return "";
	}

	private static String printSource(Site source)
	{
		return source != null ? source.getDisplayNameOrName() : "";
	}

	private static String printReleases(Collection<Release> releases)
	{
		return releases.stream().map(Release::getName).collect(Collectors.joining(", "));
	}

	private static String languageToHtml(String language)
	{
		if (language != null)
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
		return "";
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
		ImmutableMap.Builder<String, Object> np = ImmutableMap.builder();
		np.put(SeasonNamer.PARAM_INCLUDE_SERIES, Boolean.FALSE);
		np.put(SeasonNamer.PARAM_ALWAYS_INCLUDE_TITLE, Boolean.TRUE);
		np.put(EpisodeNamer.PARAM_INCLUDE_SERIES, Boolean.FALSE);
		np.put(EpisodeNamer.PARAM_INCLUDE_SEASON, Boolean.FALSE);
		np.put(EpisodeNamer.PARAM_ALWAYS_INCLUDE_TITLE, Boolean.TRUE);
		return ns.name(obj, np.build());
	}

	private SeasonPostData readSeasonPostData(int seasonThreadId) throws SQLException
	{
		try (MigrationService service = new MigrationService(assistance.getSettings()))
		{
			return service.readSeasonPost(seasonThreadId);
		}
	}

	@Override
	public void destroy()
	{

	}
}