package com.mkl.eu.service.webapp;

import org.apache.commons.io.IOUtils;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.SqlScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.sql.Connection;

/**
 * Callback flyway that always run these scripts on startup.
 *
 * @author MKL.
 */
public class EuFlywayCallback implements FlywayCallback {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FlywayCallback.class);

    /** Scripts to run on startup. */
    private Resource[] scripts;

    @Override
    public void beforeClean(Connection connection) {

    }

    @Override
    public void afterClean(Connection connection) {

    }

    @Override
    public void beforeMigrate(Connection connection) {

    }

    @Override
    public void afterMigrate(Connection connection) {
        DbSupport dbSupport = DbSupportFactory.createDbSupport(connection, false);

        for (Resource script : scripts) {
            try {
                LOG.info("Running SQL script " + script.getFilename());
                SqlScript sqlScript = new SqlScript(IOUtils.toString(script.getInputStream()), dbSupport);
                sqlScript.execute(new JdbcTemplate(connection, 0));
            } catch (IOException e) {
                throw new FlywayException("Error when running SQL script " + script.getFilename(), e);
            }
        }
    }

    @Override
    public void beforeEachMigrate(Connection connection, MigrationInfo info) {

    }

    @Override
    public void afterEachMigrate(Connection connection, MigrationInfo info) {

    }

    @Override
    public void beforeValidate(Connection connection) {

    }

    @Override
    public void afterValidate(Connection connection) {

    }

    @Override
    public void beforeBaseline(Connection connection) {

    }

    @Override
    public void afterBaseline(Connection connection) {

    }

    @Override
    public void beforeInit(Connection connection) {

    }

    @Override
    public void afterInit(Connection connection) {

    }

    @Override
    public void beforeRepair(Connection connection) {

    }

    @Override
    public void afterRepair(Connection connection) {

    }

    @Override
    public void beforeInfo(Connection connection) {

    }

    @Override
    public void afterInfo(Connection connection) {

    }

    /**
     * @param scripts {@link EuFlywayCallback#scripts}
     */
    public void setScripts(Resource[] scripts) {
        this.scripts = (scripts == null) ? null : scripts.clone();
    }
}
