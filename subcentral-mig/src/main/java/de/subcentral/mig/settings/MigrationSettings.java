package de.subcentral.mig.settings;

public class MigrationSettings {
	private MigrationEnvironmentSettings	environmentSettings	= new MigrationEnvironmentSettings();
	private MigrationParsingSettings		parsingSettings		= new MigrationParsingSettings();
	private MigrationScopeSettings			scopeSettings		= new MigrationScopeSettings();

	public MigrationEnvironmentSettings getEnvironmentSettings() {
		return environmentSettings;
	}

	public MigrationParsingSettings getParsingSettings() {
		return parsingSettings;
	}

	public MigrationScopeSettings getScopeSettings() {
		return scopeSettings;
	}
}
