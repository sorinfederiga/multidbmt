package com.company.multidbmt.security;

import com.company.multidbmt.entity.Customer;
import com.company.multidbmt.entity.Tenant;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "TenantUserRole", code = TenantUserRole.CODE, scope = "UI")
public interface TenantUserRole {
    String CODE = "tenant-user-role";

    @MenuPolicy(menuIds = "Customer.list")
    @ViewPolicy(viewIds = {"Customer.list", "Customer.detail", "DatabaseSelector"})
    void screens();

    @EntityAttributePolicy(entityClass = Customer.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Customer.class, actions = EntityPolicyAction.ALL)
    void customer();

    @EntityAttributePolicy(entityClass = Tenant.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Tenant.class, actions = EntityPolicyAction.READ)
    void tenant();
}