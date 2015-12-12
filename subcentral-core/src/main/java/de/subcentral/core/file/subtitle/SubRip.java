package de.subcentral.core.file.subtitle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import de.subcentral.core.util.IOUtil;

public class SubRip implements SubtitleFileFormat
{
	public static final SubRip		INSTANCE		= new SubRip();

	private static final Pattern	PATTERN_NUM		= Pattern.compile("\\s*\\d+\\s*");
	private static final Pattern	PATTERN_TIMINGS	= Pattern.compile("\\s*(\\d+):(\\d+):(\\d+),(\\d+)\\s*-->\\s*(\\d+):(\\d+):(\\d+),(\\d+)\\s*");

	private SubRip()
	{

	}

	@Override
	public String getName()
	{
		return "SubRip";
	}

	@Override
	public String getExtension()
	{
		return "srt";
	}

	@Override
	public String getContentType()
	{
		return "text/plain";
	}

	@Override
	public SubtitleContent read(BufferedReader reader) throws IOException
	{
		try
		{
			/**
			 * <pre>
			 * 1
			 * 00:00:03,799 --> 00:00:05,679
			 * - Here we go.
			 * - Hey, good morning.
			 * </pre>
			 */
			Matcher numMatcher = PATTERN_NUM.matcher("");
			Matcher timingsMatcher = PATTERN_TIMINGS.matcher("");
			String lastNumLine = null;
			long start = -1L;
			long end = -1L;
			final List<String> textLines = new ArrayList<>();
			final List<Item> items = new ArrayList<>();
			boolean firstLine = true;
			for (;;)
			{
				String line = reader.readLine();
				if (line == null)
				{
					// if at the end of the stream, add the last item that was read (if any)
					if (start != -1L)
					{
						addItem(items, start, end, textLines);
					}
					break;
				}
				if (firstLine)
				{
					// remove the UTF-8 BOM that may appear at the start of the content
					line = IOUtil.removeUTF8BOM(line);
					firstLine = false;
				}
				if (numMatcher.reset(line).matches())
				{
					if (lastNumLine != null)
					{
						// if there already was a num line before this num line, that line is considered to be text
						textLines.add(lastNumLine);
					}
					lastNumLine = line;
				}
				else if (timingsMatcher.reset(line).matches() && lastNumLine != null)
				{
					// add previous item if new item is encountered
					if (start != -1L)
					{
						addItem(items, start, end, textLines);
					}
					// reset
					lastNumLine = null;
					start = parseTimepoint(timingsMatcher.group(1), timingsMatcher.group(2), timingsMatcher.group(3), timingsMatcher.group(4));
					end = parseTimepoint(timingsMatcher.group(5), timingsMatcher.group(6), timingsMatcher.group(7), timingsMatcher.group(8));
					textLines.clear();
				}
				else if (StringUtils.isBlank(line))
				{
					// blank lines are interpreted as text but the lastNum is not reset
					// because blank lines also may appear between num line and timings line
					textLines.add(line);
				}
				else
				{
					if (lastNumLine != null)
					{
						// if there is a num line (last line) followed by text (this line),
						// that num line is considered to be text and not as an item number
						textLines.add(lastNumLine);
						lastNumLine = null;
					}
					textLines.add(line);
				}
			}

			SubtitleContent sub = new SubtitleContent();
			sub.setItems(items);
			return sub;
		}
		finally
		{
			reader.close();
		}
	}

	private static long parseTimepoint(String hours, String minutes, String seconds, String milliseconds)
	{
		return TimeUnit.HOURS.toMillis(Long.parseLong(hours)) + TimeUnit.MINUTES.toMillis(Long.parseLong(minutes)) + TimeUnit.SECONDS.toMillis(Long.parseLong(seconds)) + Long.parseLong(milliseconds);
	}

	private static String joinText(List<String> textLines)
	{
		boolean reachedNonBlankLine = false;
		StringBuilder sb = new StringBuilder();
		// iterate from end to start to skip all trailing blank lines
		for (int i = textLines.size() - 1; i >= 0; i--)
		{
			String line = textLines.get(i);
			if (reachedNonBlankLine || StringUtils.isNotBlank(line))
			{
				reachedNonBlankLine = true;
				// append \n after insertion if there already was a line (content in StringBuilder)
				if (sb.length() > 0)
				{
					sb.insert(0, '\n');
				}
				sb.insert(0, line);
			}
		}
		return sb.toString();
	}

	private static void addItem(List<Item> items, long start, long end, List<String> textLines)
	{
		Item item = new Item();
		item.setStart(start);
		item.setEnd(end);
		item.setText(joinText(textLines));
		items.add(item);
	}

	@Override
	public void write(SubtitleContent sub, BufferedWriter writer) throws IOException
	{
		try
		{
			/**
			 * <pre>
			 * 1
			 * 00:00:03,799 --> 00:00:05,679
			 * - Here we go.
			 * - Hey, good morning.
			 * </pre>
			 */
			for (int i = 0; i < sub.getItems().size(); i++)
			{
				Item item = sub.getItems().get(i);
				writer.write(Integer.toString(i + 1));
				writer.newLine();
				Object[] args = new Object[8];
				int argsIndex = 0;
				for (long arg : splitIntoHoursMinsSecsMillis(item.getStart()))
				{
					args[argsIndex++] = arg;
				}
				for (long arg : splitIntoHoursMinsSecsMillis(item.getEnd()))
				{
					args[argsIndex++] = arg;
				}
				writer.write(String.format("%02d:%02d:%02d,%03d --> %02d:%02d:%02d,%03d", args));
				writer.newLine();
				String text = item.getText().replace("\n", SystemUtils.LINE_SEPARATOR);
				writer.write(text);
				if (i < sub.getItems().size() - 1)
				{
					writer.newLine();
					writer.newLine();
				}
			}
		}
		finally
		{
			writer.close();
		}
	}

	private static long[] splitIntoHoursMinsSecsMillis(long millis)
	{
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
		millis -= TimeUnit.SECONDS.toMillis(seconds);
		return new long[] { hours, minutes, seconds, millis };
	}
}
