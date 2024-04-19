# Jmix Multi-database Multitenancy Sample

This example demonstrates possible implementation of multitenancy application storing tenants data in separate databases (database per tenant).

## Overview

The application has two data stores:
1. The main data store contains data shared between all tenants. This includes the list of users and tenants.
2. The additional datastore called `tenant` provides access to tenant databases.

The `tenant` data store uses [RoutingDatasource](src/main/java/com/company/multidbmt/multitenancy/RoutingDatasource.java) which creates connections to databases provided by [DataSourceRepository](src/main/java/com/company/multidbmt/multitenancy/DataSourceRepository.java). The `DataSourceRepository` class determines which database to use based on the tenant name stored in the current user session. If the current session has no information about a tenant, the default database is used. 

The [user entity](src/main/java/com/company/multidbmt/entity/User.java) has a direct link to a single tenant. This reference is used to save the tenant name in the current session upon login, see `setCurrentTenantInSession()` method of [LoginView](src/main/java/com/company/multidbmt/view/login/LoginView.java).  

The [TenantDatabaseManager](src/main/java/com/company/multidbmt/multitenancy/TenantDatabaseManager.java) class provides methods to create and drop tenant databases and run Liquibase changelogs for them. It runs Liquibase for all registered tenant databases on the application start.

Tenant databases are created on a PostgreSQL server.

## Usage

- Make sure you have a local PostgreSQL server installed.
- Open the project in IntelliJ with the Jmix plugin installed.
- Execute the **Recreate** action for **Main Data Store** and **tenant** data store to create the main shared database (`multidbmt_main`) and the default tenant database (`multidbmt_tenant_default`). The application configuration assumes that there is `root / root` user with admin privileges in the local PostgreSQL. Either create this user or change the application properties to use another user.
- Run the application and login as `admin / admin`.
- Open **Tenants** view and create a few tenants. Set different database connection parameters for each tenant.
- In the tenants list, click **Create database** for each tenant. The application will create a corresponding database and run the `tenant` data store Liquibase changelogs to create the schema.
- Open **Users** and create a few users. Set different tenants for them and assign `TenantUserRole` and `UI: minimal access` roles to them.
- Log in as different tenant users and check that data entered in the **Customers** view is stored in the tenant's database.   
- When a non-tenant user such as `admin` works with the `Customer` entity, data is stored in the default tenant database (`multidbmt_tenant_default`).