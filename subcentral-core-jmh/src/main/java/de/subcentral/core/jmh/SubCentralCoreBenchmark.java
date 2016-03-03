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

import de.subcentral.core.correct.CorrectionDefaults;
import de.subcentral.core.correct.CorrectionService;
import de.subcentral.core.correct.LocaleLanguageReplacer;
import de.subcentral.core.correct.LocaleLanguageReplacer.LanguageFormat;
import de.subcentral.core.correct.LocaleLanguageReplacer.LanguagePattern;
import de.subcentral.core.correct.SeriesNameCorrector;
import de.subcentral.core.correct.SubtitleLanguageCorrector;
import de.subcentral.core.correct.TypeBasedCorrectionService;
import de.subcentral.core.file.subtitle.SubRip;
import de.subcentral.core.file.subtitle.SubtitleContent;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.name.NamingDefaults;
import de.subcentral.core.name.NamingService;
import de.subcentral.core.parse.MultiParsingService;
import de.subcentral.core.parse.ParsingService;
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
public class SubCentralCoreBenchmark
{
	private static final SubtitleRelease	SUB_RLS						= SubtitleRelease
			.create(Release.create(Episode.createSeasonedEpisode("Psych (2001)", 8, 1), "NtbHD", "720p", "WEB", "DL", "DD5", "1", "H", "264"), "English", "SubCentral");

	private static final CorrectionService	CORRECTION_SERVICE			= buildCorrectionService();
	private static final NamingService		NAMING_SERVICE				= NamingDefaults.getDefaultNamingService();
	private static final ParsingService		ADDIC7ED_PARSING_SERVICE	= Addic7edCom.getParsingService();
	private static final ParsingService		SUBCENTRAL_PARSING_SERVICE	= SubCentralDe.getParsingService();
	private static final ParsingService		PARSING_SERVICE_BEST_CASE	= new MultiParsingService("bestcase",
																				ADDIC7ED_PARSING_SERVICE,
																				SUBCENTRAL_PARSING_SERVICE,
																				ItalianSubsNet.getParsingService(),
																				ReleaseScene.getParsingService());
	private static final ParsingService		PARSING_SERVICE_WORST_CASE	= new MultiParsingService("worstcase",
																				ReleaseScene.getParsingService(),
																				ItalianSubsNet.getParsingService(),
																				SUBCENTRAL_PARSING_SERVICE,
																				ADDIC7ED_PARSING_SERVICE);
	private static final URL				SUBRIP_TEST_FILE			= Resources.getResource("Psych.S08E10.The.Break.Up.HDTV.x264-EXCELLENCE.de-SubCentral.srt");

	private static TypeBasedCorrectionService buildCorrectionService()
	{
		TypeBasedCorrectionService service = new TypeBasedCorrectionService("testing");
		CorrectionDefaults.registerAllDefaultNestedBeansRetrievers(service);
		CorrectionDefaults.registerAllDefaultCorrectors(service);
		service.registerCorrector(Subtitle.class,
				new SubtitleLanguageCorrector(new LocaleLanguageReplacer(ImmutableList.of(Locale.ENGLISH),
						LanguageFormat.NAME,
						Locale.ENGLISH,
						ImmutableList.of(new LanguagePattern(Pattern.compile("VO", Pattern.CASE_INSENSITIVE), Locale.ENGLISH),
								new LanguagePattern(Pattern.compile("VF", Pattern.CASE_INSENSITIVE), Locale.FRENCH)),
						ImmutableMap.of(Locale.ENGLISH, "VO"))));
		service.registerCorrector(Series.class, new SeriesNameCorrector(Pattern.compile("Psych\\s+\\(2001\\)"), "Psych"));
		return service;
	}

	@Benchmark
	// @BenchmarkMode(Mode.Throughput)
	// @OutputTimeUnit(TimeUnit.NANOSECONDS)
	public void testCorrect()
	{
		CORRECTION_SERVICE.correct(SUB_RLS);
	}

	@Benchmark
	public void testNaming()
	{
		NAMING_SERVICE.name(SUB_RLS);
	}

	// @Benchmark
	public void testParsingAddic7ed()
	{
		ADDIC7ED_PARSING_SERVICE.parse("Psych - 08x01 - Episode Title.720p.WEB-DL.DD5.1H.264.English.C.orig.Addic7ed.com");
	}

	// @Benchmark
	public void testParsingSubCentral()
	{
		SUBCENTRAL_PARSING_SERVICE.parse("Psych.S08E01.720p.WEB-DL.DD5.1.H.264-ECI.de-SubCentral");
	}

	// @Benchmark
	public void testParsingBestCase()
	{
		PARSING_SERVICE_BEST_CASE.parse("Psych - 08x01 - Episode Title.720p.WEB-DL.DD5.1H.264.English.C.orig.Addic7ed.com");
	}

	// @Benchmark
	public void testParsingWorstCase()
	{
		PARSING_SERVICE_WORST_CASE.parse("Psych - 08x01 - Episode Title.720p.WEB-DL.DD5.1H.264.English.C.orig.Addic7ed.com");
	}

	// @Benchmark
	public void testParsingSubAdjBestCase()
	{
		PARSING_SERVICE_BEST_CASE.parse("Psych - 08x01 - Episode Title.720p.WEB-DL.DD5.1H.264.English.C.orig.Addic7ed.com", SubtitleRelease.class);
	}

	// @Benchmark
	public void testParsingSubAdjWorstCase()
	{
		PARSING_SERVICE_BEST_CASE.parse("Psych - 08x01 - Episode Title.720p.WEB-DL.DD5.1H.264.English.C.orig.Addic7ed.com", SubtitleRelease.class);
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
	 * PC pre staged parsing:
	 * Benchmark                                            Mode  Cnt       Score      Error  Units
	 * SubCentralCoreBenchmark.testCorrect                 thrpt   50  738975,442 ± 7549,334  ops/s
	 * SubCentralCoreBenchmark.testNaming                  thrpt   50  109221,338 ±  850,699  ops/s
	 * SubCentralCoreBenchmark.testParsingAddic7ed         thrpt   50   95612,261 ± 1746,317  ops/s
	 * SubCentralCoreBenchmark.testParsingBestCase         thrpt   50  101606,717 ±  410,947  ops/s
	 * SubCentralCoreBenchmark.testParsingSubAdjBestCase   thrpt   50  101273,923 ±  361,929  ops/s
	 * SubCentralCoreBenchmark.testParsingSubAdjWorstCase  thrpt   50  101443,277 ±  592,526  ops/s
	 * SubCentralCoreBenchmark.testParsingSubCentral       n/a
	 * SubCentralCoreBenchmark.testParsingSubRipFile       thrpt   50     813,695 ±    1,770  ops/s
	 * SubCentralCoreBenchmark.testParsingWorstCase        thrpt   50    5167,747 ±   25,768  ops/s
	 * 
	 * post staged parsing:
	 * 2016-03-03
	 * Benchmark                                            Mode  Cnt       Score      Error  Units
	 * SubCentralCoreBenchmark.testCorrect                 thrpt   50  773152,490 ± 7952,730  ops/s
	 * SubCentralCoreBenchmark.testNaming                  thrpt   50   91232,717 ± 1923,164  ops/s
	 * SubCentralCoreBenchmark.testParsingAddic7ed         thrpt   50  143362,037 ± 1614,703  ops/s
	 * SubCentralCoreBenchmark.testParsingBestCase         thrpt   50  142242,328 ± 2011,900  ops/s
	 * SubCentralCoreBenchmark.testParsingSubAdjBestCase   thrpt   50  134674,239 ± 5283,817  ops/s
	 * SubCentralCoreBenchmark.testParsingSubAdjWorstCase  thrpt   50  143176,704 ± 1365,734  ops/s
	 * SubCentralCoreBenchmark.testParsingSubCentral       thrpt   50  138436,330 ±  836,144  ops/s
	 * SubCentralCoreBenchmark.testParsingSubRipFile  	   thrpt   50     810,357 ±    6,985  ops/s
	 * SubCentralCoreBenchmark.testParsingWorstCase        thrpt   50   13854,392 ±  151,023  ops/s
	 * Laptop:
	 * Benchmark                                           Mode  Cnt       Score       Error  Units
	 * SubCentralCoreBenchmark.testCorrect                thrpt   50  570.000
	 * SubCentralCoreBenchmark.testNaming  				  thrpt   50  75225,548 ± 2985,000  ops/s
	 * SubCentralCoreBenchmark.testParsingSubAdjBestCase  thrpt   50   80k
	 * </pre>
	 * 
	 * 
	 * 
	 * @param args
	 * @throws RunnerException
	 */
	public static void main(String[] args) throws RunnerException
	{
		Options opt = new OptionsBuilder().include(SubCentralCoreBenchmark.class.getSimpleName()).forks(5).warmupIterations(15).measurementIterations(10).build();

		new Runner(opt).run();
	}
}
