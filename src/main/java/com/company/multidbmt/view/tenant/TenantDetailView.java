package com.company.multidbmt.view.tenant;

import com.company.multidbmt.entity.Tenant;
import com.company.multidbmt.view.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;

@Route(value = "tenants/:id", layout = MainView.class)
@ViewController("Tenant.detail")
@ViewDescriptor("tenant-detail-view.xml")
@EditedEntityContainer("tenantDc")
public class TenantDetailView extends StandardDetailView<Tenant> {

    @ViewComponent
    private TypedTextField<String> dbNameField;

    @Subscribe
    public void onInitEntity(final InitEntityEvent<Tenant> event) {
        Tenant tenant = event.getEntity();
        tenant.setDbHost("localhost");
        tenant.setDbPort(5432);
        tenant.setDbUser("root");
        tenant.setDbPassword("root");
    }

    @Subscribe("nameField")
    public void onNameFieldComponentValueChange(final AbstractField.ComponentValueChangeEvent<TypedTextField<String>, String> event) {
        String value = event.getValue();
        if (value != null) {
            if (StringUtils.isEmpty(dbNameField.getValue())) {
                dbNameField.setValue("multidbmt_" + value);
            }
        }
    }
}