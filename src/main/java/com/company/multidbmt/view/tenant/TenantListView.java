package com.company.multidbmt.view.tenant;

import com.company.multidbmt.multitenancy.TenantDatabaseManager;
import com.company.multidbmt.entity.Tenant;
import com.company.multidbmt.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "tenants", layout = MainView.class)
@ViewController("Tenant.list")
@ViewDescriptor("tenant-list-view.xml")
@LookupComponent("tenantsDataGrid")
@DialogMode(width = "64em")
public class TenantListView extends StandardListView<Tenant> {
    @ViewComponent
    private DataGrid<Tenant> tenantsDataGrid;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private DataManager dataManager;
    @ViewComponent
    private CollectionLoader<Tenant> tenantsDl;
    @Autowired
    private TenantDatabaseManager tenantDatabaseManager;

    @Subscribe("tenantsDataGrid.createDb")
    public void onDatabasesDataGridCreateDb(final ActionPerformedEvent event) {
        Tenant tenant = tenantsDataGrid.getSingleSelectedItem();
        if (tenant == null)
            return;

        dialogs.createOptionDialog()
                .withText("Create database " + tenant.getFullDatabaseName() + "?")
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withHandler(e ->
                                        createDatabase(tenant)),
                        new DialogAction(DialogAction.Type.NO)
                )
                .open();
    }

    @Subscribe("tenantsDataGrid.dropDb")
    public void onTenantsDataGridDropDb(final ActionPerformedEvent event) {
        Tenant tenant = tenantsDataGrid.getSingleSelectedItem();
        if (tenant == null)
            return;

        dialogs.createOptionDialog()
                .withText("Drop database " + tenant.getFullDatabaseName() + "?")
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withHandler(e ->
                                        dropDatabase(tenant))
                                .withVariant(ActionVariant.DANGER),
                        new DialogAction(DialogAction.Type.NO)
                                .withVariant(ActionVariant.PRIMARY)
                )
                .open();

    }

    private void createDatabase(Tenant tenant) {
        tenantDatabaseManager.createDatabase(
                tenant.getDbHost(), tenant.getDbPort(), tenant.getDbName(), tenant.getDbUser(), tenant.getDbPassword());

        tenant.setDbCreated(true);
        dataManager.save(tenant);
        tenantsDl.load();
    }

    @Install(to = "tenantsDataGrid.remove", subject = "enabledRule")
    private boolean tenantsDataGridRemoveEnabledRule() {
        Tenant tenant = tenantsDataGrid.getSingleSelectedItem();
        return tenant != null && !Boolean.TRUE.equals(tenant.getDbCreated());
    }

    private void dropDatabase(Tenant tenant) {
        tenantDatabaseManager.dropDatabase(
                tenant.getDbHost(), tenant.getDbPort(), tenant.getDbName(), tenant.getDbUser(), tenant.getDbPassword());

        tenant.setDbCreated(false);
        dataManager.save(tenant);
        tenantsDl.load();
    }
}