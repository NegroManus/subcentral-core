package de.subcentral.mig.check;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.mig.Migration;
import de.subcentral.mig.MigrationConfig;
import de.subcentral.mig.process.SeriesListParser;
import de.subcentral.mig.process.SeriesListParser.SeriesListContent;
import de.subcentral.mig.process.SubCentralBoard;
import de.subcentral.mig.process.SubCentralBoard.Post;
import de.subcentral.support.subcentralde.SubCentralHttpApi;

public class SanityChecker
{
	private final MigrationConfig config;

	public SanityChecker(MigrationConfig config)
	{
		this.config = config;
	}

	public void check() throws Exception
	{
		SeriesListContent seriesListContent = getSeriesListContent();
		List<Series> quickJumpContent = getQuickJumpContent();

		checkSeriesListAgainstQuickJump(seriesListContent.getSeries(), quickJumpContent);
	}

	private void checkSeriesListAgainstQuickJump(List<Series> seriesListSeries, List<Series> quickjumpSeries)
	{

	}

	private SeriesListContent getSeriesListContent() throws SQLException
	{
		int seriesListPostId = config.getEnvironmentSettings().getInt("sc.serieslist.postid");
		SeriesListContent seriesListContent;
		try (Connection conn = config.getDataSource().getConnection())
		{
			SubCentralBoard scBoard = new SubCentralBoard();
			scBoard.setConnection(conn);
			Post seriesListPost = scBoard.getPost(seriesListPostId);
			seriesListContent = new SeriesListParser().parsePost(seriesListPost.getMessage());
			return seriesListContent;
		}
	}

	/**
	 * <pre>
	 * <form method="get" action="index.php" class="quickJump">
	 *  	<input type="hidden" name="page" value="Board" />
	 *		<select name="boardID" onchange="if (this.options[this.selectedIndex].value != 0) this.form.submit()" id="QJselect">
	 *			<option value=""> Serien-QuickJump </option>
	 *			<optgroup label="-- 0-9 --">
	 *				<option value="427">10 Things I Hate About You</option>
	 * </pre>
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<Series> getQuickJumpContent() throws IOException
	{
		SubCentralHttpApi api = new SubCentralHttpApi();
		Document mainPage = api.getContent("index.php");
		Element quickJumpSelect = mainPage.getElementById("QJselect");
		if (quickJumpSelect == null)
		{
			throw new IllegalStateException("Quickjump form could not be found");
		}
		Elements options = quickJumpSelect.getElementsByTag("option");
		// "- 1" because one option is the empty value
		List<Series> seriesList = new ArrayList<>(options.size() - 1);
		for (Element option : options)
		{
			String value = option.attr("value");
			if (value.isEmpty())
			{
				continue;
			}
			String seriesName = option.text();
			Integer boardId = Integer.valueOf(value);
			Series series = new Series(seriesName);
			series.addAttributeValue(Migration.SERIES_ATTR_BOARD_ID, boardId);
			seriesList.add(series);
		}
		return ImmutableList.copyOf(seriesList);
	}
}
