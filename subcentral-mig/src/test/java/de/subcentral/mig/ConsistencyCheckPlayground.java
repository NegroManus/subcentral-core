package de.subcentral.mig;

import java.nio.file.Paths;

import org.junit.Test;

import com.google.common.io.Resources;

import de.subcentral.mig.check.ConsistencyChecker;
import de.subcentral.mig.process.MigrationAssistance;

public class ConsistencyCheckPlayground
{
	@Test
	public void testConsistencyCheck() throws Exception
	{
		MigrationAssistance assistance = new MigrationAssistance();
		assistance.setEnvironmentSettingsFile(Paths.get(Resources.getResource(ConsistencyCheckPlayground.class, "/de/subcentral/mig/migration-env-settings.properties").toURI()));
		assistance.loadEnvironmentSettingsFromFile();
		ConsistencyChecker checker = new ConsistencyChecker(assistance.getSettings());
		checker.check();
	}
}
