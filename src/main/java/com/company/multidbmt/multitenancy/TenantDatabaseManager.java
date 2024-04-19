package com.company.multidbmt.multitenancy;

import com.company.multidbmt.entity.Tenant;
import io.jmix.core.DataManager;
import io.jmix.core.Resources;
import io.jmix.core.security.Authenticated;
import io.jmix.core.security.SystemAuthenticator;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Contains methods to create, run Liquibase and drop tenant databases.
 * On the application startup, runs Liquibase for all registered tenants.
 */
@Component
public class TenantDatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(TenantDatabaseManager.class);

    private final Resources resources;
    private final DataManager dataManager;

    public TenantDatabaseManager(Resources resources, DataManager dataManager) {
        this.resources = resources;
        this.dataManager = dataManager;
    }

    public void createDatabase(String dbHost, Integer dbPort, String dbName, String dbUser, String dbPassword) {
        String url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/postgres";
        log.info("Creating database " + url);
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
             Statement statement = connection.createStatement()) {
            statement.execute("create database " + dbName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        runLiquibase(dbHost, dbPort, dbName, dbUser, dbPassword);
    }

    public void dropDatabase(String dbHost, Integer dbPort, String dbName, String dbUser, String dbPassword) {
        String url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/postgres";
        log.info("Dropping database " + url);
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
             Statement statement = connection.createStatement()) {
            statement.execute("drop database " + dbName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void runLiquibase(String dbHost, Integer dbPort, String dbName, String dbUser, String dbPassword) {
        String url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
        log.info("Running Liquibase for " + url);
        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
            Liquibase liquibase = new Liquibase(
                    "com/company/multidbmt/liquibase/tenant-changelog.xml",
                    new SpringResourceAccessor(resources),
                    new JdbcConnection(connection));
            liquibase.update();
        } catch (SQLException | LiquibaseException e) {
            throw new RuntimeException(e);
        }
    }

    @Authenticated
    @EventListener
    public void onApplicationStarted(final ApplicationStartedEvent event) {
        dataManager.load(Tenant.class).all().list().forEach(tenant -> {
            if (Boolean.TRUE.equals(tenant.getDbCreated())) {
                runLiquibase(tenant.getDbHost(), tenant.getDbPort(), tenant.getDbName(), tenant.getDbUser(), tenant.getDbPassword());
            }
        });
    }
}