package com.company.multidbmt.view.main;

import com.company.multidbmt.entity.Tenant;
import com.company.multidbmt.entity.User;
import com.company.multidbmt.multitenancy.DataSourceRepository;
import com.company.multidbmt.view.databaseselector.DatabaseSelector;
import com.company.multidbmt.view.login.LoginView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.session.SessionData;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Route("")
@ViewController("MainView")
@ViewDescriptor("main-view.xml")
public class MainView extends StandardMainView {

    @Autowired
    private CurrentAuthentication currentAuthentication;
    @ViewComponent
    private Span tenantBadge;
    @Autowired
    private DialogWindows dialogWindows;
    @Autowired
    private SessionData sessionData;
    @ViewComponent
    private JmixButton cmdSwichDatabase;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        Object tenant = sessionData.getAttribute(DataSourceRepository.TENANT_NAME_SESSION_ATTR);
        if (tenant==null && !currentAuthentication.getUser().getUsername().equals("admin") )  {
            runSwichDatabase();
        }
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        cmdSwichDatabase.setEnabled(!currentAuthentication.getUser().getUsername().equals("admin"));
    }

    private void runSwichDatabase() {

        DialogWindow<DatabaseSelector> window =
                dialogWindows.view(this, DatabaseSelector.class).build();

        window.addAfterCloseListener(afterCloseEvent -> {
            Tenant tenant = window.getView().getTenant();
                tenantBadge.setText(tenant.getName());

        });
        window.open();
    }

    @Subscribe(id = "cmdSwichDatabase", subject = "clickListener")
    public void onCmdSwichDatabaseClick(final ClickEvent<JmixButton> event) {
        runSwichDatabase();
    }





}
