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

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.standardizing.ClassBasedStandardizingService;
import de.subcentral.core.standardizing.LocaleBasedSubtitleLanguageStandardizer;
import de.subcentral.core.standardizing.LocaleBasedSubtitleLanguageStandardizer.LanguageFormat;
import de.subcentral.core.standardizing.StandardizingDefaults;
import de.subcentral.support.addic7edcom.Addic7edCom;

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
	private static final SubtitleAdjustment				SUB_ADJ						= SubtitleAdjustment.create(Release.create(Episode.createSeasonedEpisode("Psych",
																							8,
																							1),
																							"NtbHD",
																							"720p",
																							"WEB",
																							"DL",
																							"DD5",
																							"1",
																							"H",
																							"264"),
																							"English",
																							"SubCentral");

	private static final ClassBasedStandardizingService	STANDARDIZING_SERVICE		= buildService();
	private static final NamingService					NAMING_SERVICE				= NamingDefaults.getDefaultNamingService();
	private static final ParsingService					ADDIC7ED_PARSING_SERVICE	= Addic7edCom.getParsingService();

	private static ClassBasedStandardizingService buildService()
	{
		ClassBasedStandardizingService service = new ClassBasedStandardizingService("testing");
		StandardizingDefaults.registerAllDefaultNestedBeansRetrievers(service);
		StandardizingDefaults.registerAllDefaulStandardizers(service);
		service.registerStandardizer(Subtitle.class,
				new LocaleBasedSubtitleLanguageStandardizer(ImmutableList.of(Locale.ENGLISH),
						LanguageFormat.NAME,
						Locale.ENGLISH,
						ImmutableMap.of(Pattern.compile("VO", Pattern.CASE_INSENSITIVE),
								Locale.ENGLISH,
								Pattern.compile("VF", Pattern.CASE_INSENSITIVE),
								Locale.FRENCH),
						ImmutableMap.of(Locale.ENGLISH, "VO")));
		return service;
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public void testStandardizing()
	{
		STANDARDIZING_SERVICE.standardize(SUB_ADJ);
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public void testNaming()
	{
		NAMING_SERVICE.name(SUB_ADJ);
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public void testParsingAddic7ed()
	{
		ADDIC7ED_PARSING_SERVICE.parse("Psych - 08x01 - Episode Title.720p.WEB-DL.DD5.1H.264.English.C.orig.Addic7ed.com");
	}

	/**
	 * http://openjdk.java.net/projects/code-tools/jmh/
	 * 
	 * Last results
	 * 
	 * <pre>
	 * Benchmark                        Mode  Cnt     Score     Error  Units
	 * MyBenchmark.testNaming           avgt   10  8802,149 ± 741,668  ns/op
	 * MyBenchmark.testParsingAddic7ed  avgt   10  9432,883 ± 547,290  ns/op
	 * MyBenchmark.testStandardizing    avgt   10  1621,994 ±  58,712  ns/op
	 * </pre>
	 * 
	 * @param args
	 * @throws RunnerException
	 */
	public static void main(String[] args) throws RunnerException
	{
		Options opt = new OptionsBuilder().include(MyBenchmark.class.getSimpleName()).warmupIterations(10).measurementIterations(10).forks(1).build();

		new Runner(opt).run();
	}
}
