package com.company.multidbmt;

import com.company.multidbmt.multitenancy.DataSourceRepository;
import com.company.multidbmt.multitenancy.RoutingDatasource;
import io.jmix.autoconfigure.data.JmixLiquibaseCreator;
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.data.impl.JmixEntityManagerFactoryBean;
import io.jmix.data.impl.JmixTransactionManager;
import io.jmix.data.persistence.DbmsSpecifics;
import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;

/**
 * Instantiates beans to access the tenant databases.
 */
@Configuration
public class TenantStoreConfiguration {

    @Bean
    @ConfigurationProperties("tenant.datasource")
    DataSourceProperties tenantDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "tenant.datasource.hikari")
    DataSourceRepository datasourceRepository(@Qualifier("tenantDataSourceProperties") DataSourceProperties properties) {
        DataSource defaultDatasource = properties.initializeDataSourceBuilder().build();
        return new DataSourceRepository(defaultDatasource);
    }

    @Bean
    DataSource tenantDataSource(DataSourceRepository datasourceRepository) {
        return new RoutingDatasource(datasourceRepository);
    }

    @Bean
    LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory(
            @Qualifier("tenantDataSource") DataSource dataSource,
            JpaVendorAdapter jpaVendorAdapter,
            DbmsSpecifics dbmsSpecifics,
            JmixModules jmixModules,
            Resources resources
    ) {
        return new JmixEntityManagerFactoryBean("tenant", dataSource, jpaVendorAdapter, dbmsSpecifics, jmixModules, resources);
    }

    @Bean
    JpaTransactionManager tenantTransactionManager(@Qualifier("tenantEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JmixTransactionManager("tenant", entityManagerFactory);
    }

    @Bean("tenantLiquibaseProperties")
    @ConfigurationProperties(prefix = "tenant.liquibase")
    public LiquibaseProperties tenantLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean("tenantLiquibase")
    public SpringLiquibase tenantLiquibase(@Qualifier("tenantDataSource") DataSource dataSource,
                                           @Qualifier("tenantLiquibaseProperties") LiquibaseProperties liquibaseProperties) {
        return JmixLiquibaseCreator.create(dataSource, liquibaseProperties);
    }
}
