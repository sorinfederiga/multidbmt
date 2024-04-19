package com.company.multidbmt.multitenancy;

import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Returns a connection to a database from a {@link DataSource} returned by {@link DataSourceRepository}.
 * <p>
 * The bean is instantiated in {@link com.company.multidbmt.TenantStoreConfiguration}.
 */
public class RoutingDatasource extends AbstractDataSource {

    private final DataSourceRepository datasourceRepository;

    public RoutingDatasource(DataSourceRepository datasourceRepository) {
        this.datasourceRepository = datasourceRepository;
    }

    @Override
    public Connection getConnection() throws SQLException {
        DataSource dataSource = datasourceRepository.getDatasource();
        return dataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }
}
