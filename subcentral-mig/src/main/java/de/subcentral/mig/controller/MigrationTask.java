package de.subcentral.mig.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.mig.SubCentralSubMan;
import de.subcentral.mig.process.MigrationService;
import de.subcentral.mig.settings.MigrationSettings;
import javafx.concurrent.Task;

class MigrationTask extends Task<Void> {
    private static final Logger     log = LogManager.getLogger(MigrationTask.class);

    private final MigrationSettings settings;
    private MigrationService        service;

    public MigrationTask(MigrationSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    @Override
    protected Void call() throws Exception {
        updateTitle("Migration");
        try {
            service = new MigrationService(settings);
            migrate();
            return null;
        }
        finally {
            if (service != null) {
                service.close();
            }
        }
    }

    private void migrate() throws Exception {
        List<Series> includedSeries = getIncludedSeries();
        insertSeries(includedSeries);
    }

    @Override
    protected void succeeded() {
        updateMessage("Done");
        updateProgress(1L, 1L);
    }

    @Override
    protected void cancelled() {
        updateMessage("Cancelled");
        updateProgress(1L, 1L);
    }

    @Override
    protected void failed() {
        updateMessage("Failed");
        updateProgress(1L, 1L);
    }

    private List<Series> getIncludedSeries() throws SQLException {
        if (settings.getScopeSettings().getIncludeAllSeries()) {
            return service.readSeriesList().getSeries();
        }
        return settings.getScopeSettings().getIncludedSeries();
    }

    private void insertSeries(List<Series> series) throws SQLException {
        log.info("Inserting {} series into the target database", series.size());
        try (Connection conn = service.getTargetDataSource().getConnection()) {
            SubCentralSubMan subMan = new SubCentralSubMan(conn);
            for (Series s : series) {
                subMan.insertSeriesFromSeriesList(s);
            }
        }
    }
}