package de.subcentral.mig;

import java.nio.file.Paths;

import org.junit.Test;

import com.google.common.io.Resources;

import de.subcentral.mig.check.SanityChecker;

public class SanityCheckPlayground
{
	@Test
	public void testSanityCheck() throws Exception
	{
		MigrationConfig cfg = new MigrationConfig();
		cfg.setEnvironmentSettingsFile(Paths.get(Resources.getResource(SanityCheckPlayground.class, "/de/subcentral/mig/migration-env-settings.properties").toURI()));
		cfg.loadEnvironmentSettings();
		cfg.createDateSource();

		SanityChecker checker = new SanityChecker(cfg);
		checker.check();
	}
}
