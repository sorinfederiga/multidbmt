package com.company.multidbmt.view.databaseselector;


import com.company.multidbmt.entity.Tenant;
import com.company.multidbmt.entity.User;
import com.company.multidbmt.multitenancy.DataSourceRepository;
import com.company.multidbmt.view.main.MainView;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.component.listbox.JmixListBox;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "database-selector", layout = MainView.class)
@ViewController("DatabaseSelector")
@ViewDescriptor("database-selector.xml")
public class DatabaseSelector extends StandardView {
    private static final Logger log = LoggerFactory.getLogger(DatabaseSelector.class);
    @ViewComponent
    private JmixListBox<Object> lstTenant;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @ViewComponent
    private CollectionLoader<User> tenantDl;
    @Autowired
    private SessionData sessionData;
    private Tenant tenant;
    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        final User user = (User) currentAuthentication.getUser();
        tenantDl.setParameter("user",user);
        tenantDl.load();
    }

    @Subscribe(id = "closeBtn", subject = "singleClickListener")
    public void onCloseBtnClick(final ClickEvent<JmixButton> event) {
        close(StandardOutcome.CLOSE);
    }

    @Subscribe("lstTenant")
    public void onLstTenantComponentValueChange(final AbstractField.ComponentValueChangeEvent<JmixListBox<Tenant>, Tenant> event) {
        setTenant(event.getValue());
    }


    @Subscribe
    public void onAfterClose(final AfterCloseEvent event) {
        Tenant tenant = (Tenant) lstTenant.getValue();
        setCurrentTenantInSession(tenant);
    }

    private void setCurrentTenantInSession(Tenant tenant) {
        if (tenant != null) {
            log.info("Setting tenant " + tenant.getName() + " in current session");
            sessionData.setAttribute(DataSourceRepository.TENANT_NAME_SESSION_ATTR, tenant.getName());
        }
    }


    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }
}