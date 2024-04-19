package com.company.multidbmt.multitenancy;

import com.company.multidbmt.entity.Tenant;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.session.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.request.RequestContextHolder;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean that initializes, stores and returns a {@link DataSource} for the {@link Tenant} associated with the current
 * user session. If the tenant is not set for the session, returns the default datasource.
 * <p>
 * Instantiated in {@link com.company.multidbmt.TenantStoreConfiguration}.
 */
public class DataSourceRepository implements ApplicationContextAware {

    private final static Logger log = LoggerFactory.getLogger(DataSourceRepository.class);

    public static final String TENANT_NAME_SESSION_ATTR = "MDBMT_TENANT";

    private final Map<String, DataSource> tenantToDatasourceMap = new ConcurrentHashMap<>();

    private final DataSource defaultDataSource;
    private ObjectProvider<SessionData> sessionDataProvider;
    private UnconstrainedDataManager dataManager;

    public DataSourceRepository(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.sessionDataProvider = applicationContext.getBeanProvider(SessionData.class);
        this.dataManager = applicationContext.getBean(UnconstrainedDataManager.class);
    }

    public DataSource getDatasource() {
        if (RequestContextHolder.getRequestAttributes() == null) {
            log.debug("Not in a session scope");
            return getDefaultDataSource();
        }

        String tenantName = (String) sessionDataProvider.getObject().getAttribute(TENANT_NAME_SESSION_ATTR);
        if (tenantName == null) {
            log.debug("Tenant is not set for the current session");
            return getDefaultDataSource();
        }

        return tenantToDatasourceMap.computeIfAbsent(tenantName, this::createDataSource);
    }

    public DataSource getDefaultDataSource() {
        return defaultDataSource;
    }

    private DataSource createDataSource(String tenantName) {
        Tenant tenant = dataManager.load(Tenant.class)
                .query("e.name = ?1", tenantName)
                .optional()
                .orElseThrow(() -> new RuntimeException("Cannot find tenant " + tenantName));

        log.info("Creating DataSource for {}", tenant.getFullDatabaseName());
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(tenant.getDatabaseUrl());
        hikariConfig.setUsername(tenant.getDbUser());
        hikariConfig.setPassword(tenant.getDbPassword());

        return new HikariDataSource(hikariConfig);
    }
}
