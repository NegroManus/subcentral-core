package de.subcentral.mig;

import java.nio.file.Paths;

import org.junit.Test;

import com.google.common.io.Resources;

import de.subcentral.mig.check.ConsistencyChecker;

public class ConsistencyCheckPlayground
{
	@Test
	public void testConsistencyCheck() throws Exception
	{
		MigrationConfig cfg = new MigrationConfig();
		cfg.setEnvironmentSettingsFile(Paths.get(Resources.getResource(ConsistencyCheckPlayground.class, "/de/subcentral/mig/migration-env-settings.properties").toURI()));
		cfg.loadEnvironmentSettings();
		cfg.createDateSource();

		ConsistencyChecker checker = new ConsistencyChecker(cfg);
		checker.check();
	}
}
