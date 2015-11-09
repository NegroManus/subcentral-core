/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.subcentral.core.jmh;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.regex.Pattern;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;

import de.subcentral.core.correction.CorrectionDefaults;
import de.subcentral.core.correction.LocaleLanguageReplacer;
import de.subcentral.core.correction.LocaleLanguageReplacer.LanguageFormat;
import de.subcentral.core.correction.LocaleLanguageReplacer.LanguagePattern;
import de.subcentral.core.correction.LocaleSubtitleLanguageCorrector;
import de.subcentral.core.correction.TypeCorrectionService;
import de.subcentral.core.file.subtitle.SubRip;
import de.subcentral.core.file.subtitle.SubtitleContent;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleVariant;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.ParsingUtil;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.italiansubsnet.ItalianSubsNet;
import de.subcentral.support.releasescene.ReleaseScene;
import de.subcentral.support.subcentralde.SubCentralDe;

/**
 * <pre>
 * a) Via the command line:
 *    $ mvn clean install
 *    $ java -jar target/benchmarks.jar JMHSample_02 -wi 5 -i 5 -f 1
 *    (we requested 5 warmup/measurement iterations, single fork)
 * </pre>
 *
 */
public class MyBenchmark
{
	private static final SubtitleVariant SUB_ADJ = SubtitleVariant.create(Release.create(Episode.createSeasonedEpisode("Psych", 8, 1), "NtbHD", "720p", "WEB", "DL", "DD5", "1", "H", "264"),
			"English",
			"SubCentral");

	private static final TypeCorrectionService			STANDARDIZING_SERVICE		= buildService();
	private static final NamingService					NAMING_SERVICE				= NamingDefaults.getDefaultNamingService();
	private static final ParsingService					ADDIC7ED_PARSING_SERVICE	= Addic7edCom.getParsingService();
	private static final ImmutableList<ParsingService>	PARSING_SERVICES			= ImmutableList.of(ADDIC7ED_PARSING_SERVICE,
			SubCentralDe.getParsingService(),
			ItalianSubsNet.getParsingService(),
			ReleaseScene.getParsingService());
	private static final ImmutableList<ParsingService>	PARSING_SERVICES_REVERSED	= ImmutableList.of(ReleaseScene.getParsingService(),
			ItalianSubsNet.getParsingService(),
			SubCentralDe.getParsingService(),
			ADDIC7ED_PARSING_SERVICE);
	private static final URL							SUBRIP_TEST_FILE			= Resources.getResource("Psych.S08E10.The.Break.Up.HDTV.x264-EXCELLENCE.de-SubCentral.srt");

	private static TypeCorrectionService buildService()
	{
		TypeCorrectionService service = new TypeCorrectionService("testing");
		CorrectionDefaults.registerAllDefaultNestedBeansRetrievers(service);
		CorrectionDefaults.registerAllDefaultCorrectors(service);
		service.registerStandardizer(Subtitle.class,
				new LocaleSubtitleLanguageCorrector(new LocaleLanguageReplacer(ImmutableList.of(Locale.ENGLISH),
						LanguageFormat.NAME,
						Locale.ENGLISH,
						ImmutableList.of(new LanguagePattern(Pattern.compile("VO", Pattern.CASE_INSENSITIVE), Locale.ENGLISH),
								new LanguagePattern(Pattern.compile("VF", Pattern.CASE_INSENSITIVE), Locale.FRENCH)),
						ImmutableMap.of(Locale.ENGLISH, "VO"))));
		return service;
	}

	// @Benchmark
	// @BenchmarkMode(Mode.Throughput)
	// @OutputTimeUnit(TimeUnit.NANOSECONDS)
	public void testStandardizing()
	{
		STANDARDIZING_SERVICE.correct(SUB_ADJ);
	}

	// @Benchmark
	public void testNaming()
	{
		NAMING_SERVICE.name(SUB_ADJ);
	}

	// @Benchmark
	public void testParsingAddic7ed()
	{
		ADDIC7ED_PARSING_SERVICE.parse("Psych - 08x01 - Episode Title.720p.WEB-DL.DD5.1H.264.English.C.orig.Addic7ed.com");
	}

	// @Benchmark
	public void testParsingBestCase()
	{
		ParsingUtil.parse("Psych - 08x01 - Episode Title.720p.WEB-DL.DD5.1H.264.English.C.orig.Addic7ed.com", PARSING_SERVICES);
	}

	// @Benchmark
	public void testParsingWorstCase()
	{
		ParsingUtil.parse("Psych - 08x01 - Episode Title.720p.WEB-DL.DD5.1H.264.English.C.orig.Addic7ed.com", PARSING_SERVICES_REVERSED);
	}

	// @Benchmark
	public void testParsingSubAdjBestCase()
	{
		ParsingUtil.parse("Psych - 08x01 - Episode Title.720p.WEB-DL.DD5.1H.264.English.C.orig.Addic7ed.com", SubtitleVariant.class, PARSING_SERVICES);
	}

	// @Benchmark
	public void testParsingSubAdjWorstCase()
	{
		ParsingUtil.parse("Psych - 08x01 - Episode Title.720p.WEB-DL.DD5.1H.264.English.C.orig.Addic7ed.com", SubtitleVariant.class, PARSING_SERVICES_REVERSED);
	}

	@Benchmark
	public void testParsingSubRipFile(Blackhole blackhole) throws IOException
	{
		SubtitleContent data = SubRip.INSTANCE.read(SUBRIP_TEST_FILE.openStream(), Charset.forName("Cp1252"));
		blackhole.consume(data);
	}

	/**
	 * http://openjdk.java.net/projects/code-tools/jmh/
	 * 
	 * Current results:
	 * 
	 * <pre>
	 * Benchmark                                Mode  Cnt       Score      Error  Units
	 * MyBenchmark.testNaming                  thrpt   50   84270,786 ± 1888,604  ops/s
	 * MyBenchmark.testParsingAddic7ed         thrpt   50  128588,327 ± 1569,210  ops/s
	 * MyBenchmark.testParsingBestCase         thrpt   50  131312,053 ± 1883,485  ops/s
	 * MyBenchmark.testParsingSubAdjBestCase   thrpt   50  121854,993 ± 2141,183  ops/s
	 * MyBenchmark.testParsingSubAdjWorstCase  thrpt   50    8434,303 ±   71,054  ops/s
	 * MyBenchmark.testParsingWorstCase        thrpt   50    4500,268 ±   48,533  ops/s
	 * MyBenchmark.testStandardizing           thrpt   50  669034,817 ± 4591,636  ops/s
	 * 
	 * Benchmark                           Mode  Cnt    Score    Error  Units
	 * MyBenchmark.testParsingSubRipFile  thrpt   50  474,739 ± 18,451  ops/s
	 * </pre>
	 * 
	 * 
	 * 
	 * @param args
	 * @throws RunnerException
	 */
	public static void main(String[] args) throws RunnerException
	{
		Options opt = new OptionsBuilder().include(MyBenchmark.class.getSimpleName()).forks(5).warmupIterations(15).measurementIterations(10).build();

		new Runner(opt).run();
	}
}
