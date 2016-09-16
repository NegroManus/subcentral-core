package de.subcentral.mig.settings;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

public class MigrationEnvironmentSettings {
	private static final String	KEY_LOG_DIR						= "log.dir";
	private static final String	KEY_SOURCE_DB_DRIVERCLASS		= "source.db.driverclass";
	private static final String	KEY_SOURCE_DB_URL				= "source.db.url";
	private static final String	KEY_SOURCE_DB_USER				= "source.db.user";
	private static final String	KEY_SOURCE_DB_PASSWORD			= "source.db.password";
	private static final String	KEY_SOURCE_SERIESLIST_POSTID	= "source.serieslist.postid";
	private static final String	KEY_SOURCE_SUBBERLIST_POSTID	= "source.subberlist.postid";
	private static final String	KEY_SOURCE_SUBREPO_BOARDID		= "source.subrepo.boardid";
	private static final String	KEY_SOURCE_ATTACHMENTS_DIR		= "source.attachments.dir";
	private static final String	KEY_TARGET_DB_DRIVERCLASS		= "target.db.driverclass";
	private static final String	KEY_TARGET_DB_URL				= "target.db.url";
	private static final String	KEY_TARGET_DB_USER				= "target.db.user";
	private static final String	KEY_TARGET_DB_PASSWORD			= "target.db.password";
	private static final String	KEY_TARGET_SUBS_DIR				= "target.subs.dir";

	// General
	private Path				logDir;

	// Source
	// DB
	private String				sourceDbDriverClass;
	private String				sourceDbUrl;
	private String				sourceDbUser;
	private String				sourceDbPassword;

	// Other
	private int					sourceSeriesListPostId;
	private int					sourceSubberListPostId;
	private int					sourceSubRepoBoardId;
	private Path				sourceAttachmentsDir;

	// Target
	// DB
	private String				targetDbDriverClass;
	private String				targetDbUrl;
	private String				targetDbUser;
	private String				targetDbPassword;

	// Other
	private Path				targetSubsDir;

	public Path getLogDir() {
		return logDir;
	}

	public void setLogDir(Path logDir) {
		this.logDir = logDir;
	}

	public String getSourceDbDriverClass() {
		return sourceDbDriverClass;
	}

	public void setSourceDbDriverClass(String sourceDbDriverClass) {
		this.sourceDbDriverClass = sourceDbDriverClass;
	}

	public String getSourceDbUrl() {
		return sourceDbUrl;
	}

	public void setSourceDbUrl(String sourceDbUrl) {
		this.sourceDbUrl = sourceDbUrl;
	}

	public String getSourceDbUser() {
		return sourceDbUser;
	}

	public void setSourceDbUser(String sourceDbUser) {
		this.sourceDbUser = sourceDbUser;
	}

	public String getSourceDbPassword() {
		return sourceDbPassword;
	}

	public void setSourceDbPassword(String sourceDbPassword) {
		this.sourceDbPassword = sourceDbPassword;
	}

	public int getSourceSeriesListPostId() {
		return sourceSeriesListPostId;
	}

	public void setSourceSeriesListPostId(int sourceSeriesListPostId) {
		this.sourceSeriesListPostId = sourceSeriesListPostId;
	}

	public int getSourceSubberListPostId() {
		return sourceSubberListPostId;
	}

	public void setSourceSubberListPostId(int sourceSubberListPostId) {
		this.sourceSubberListPostId = sourceSubberListPostId;
	}

	public int getSourceSubRepoBoardId() {
		return sourceSubRepoBoardId;
	}

	public void setSourceSubRepoBoardId(int sourceSubRepoBoardId) {
		this.sourceSubRepoBoardId = sourceSubRepoBoardId;
	}

	public Path getSourceAttachmentsDir() {
		return sourceAttachmentsDir;
	}

	public void setSourceAttachmentsDir(Path sourceAttachmentsDir) {
		this.sourceAttachmentsDir = sourceAttachmentsDir;
	}

	public String getTargetDbDriverClass() {
		return targetDbDriverClass;
	}

	public void setTargetDbDriverClass(String targetDbDriverClass) {
		this.targetDbDriverClass = targetDbDriverClass;
	}

	public String getTargetDbUrl() {
		return targetDbUrl;
	}

	public void setTargetDbUrl(String targetDbUrl) {
		this.targetDbUrl = targetDbUrl;
	}

	public String getTargetDbUser() {
		return targetDbUser;
	}

	public void setTargetDbUser(String targetDbUser) {
		this.targetDbUser = targetDbUser;
	}

	public String getTargetDbPassword() {
		return targetDbPassword;
	}

	public void setTargetDbPassword(String targetDbPassword) {
		this.targetDbPassword = targetDbPassword;
	}

	public Path getTargetSubsDir() {
		return targetSubsDir;
	}

	public void setTargetSubsDir(Path targetSubsDir) {
		this.targetSubsDir = targetSubsDir;
	}

	public void load(Path file) throws IOException, ConfigurationException {
		PropertiesConfiguration cfg = new PropertiesConfiguration();
		FileHandler fileHandler = new FileHandler(cfg);
		fileHandler.load(Files.newBufferedReader(file, Charset.forName("UTF-8")));
		load(cfg);
	}

	public void load(PropertiesConfiguration cfg) {
		logDir = Paths.get(cfg.getString(KEY_LOG_DIR));

		sourceDbDriverClass = cfg.getString(KEY_SOURCE_DB_DRIVERCLASS);
		sourceDbUrl = cfg.getString(KEY_SOURCE_DB_URL);
		sourceDbUser = cfg.getString(KEY_SOURCE_DB_USER);
		sourceDbPassword = cfg.getString(KEY_SOURCE_DB_PASSWORD);

		sourceSeriesListPostId = cfg.getInt(KEY_SOURCE_SERIESLIST_POSTID);
		sourceSubberListPostId = cfg.getInt(KEY_SOURCE_SUBBERLIST_POSTID);
		sourceSubRepoBoardId = cfg.getInt(KEY_SOURCE_SUBREPO_BOARDID);
		sourceAttachmentsDir = Paths.get(cfg.getString(KEY_SOURCE_ATTACHMENTS_DIR));

		targetDbDriverClass = cfg.getString(KEY_TARGET_DB_DRIVERCLASS);
		targetDbUrl = cfg.getString(KEY_TARGET_DB_URL);
		targetDbUser = cfg.getString(KEY_TARGET_DB_USER);
		targetDbPassword = cfg.getString(KEY_TARGET_DB_PASSWORD);

		targetSubsDir = Paths.get(cfg.getString(KEY_TARGET_SUBS_DIR));
	}
}
