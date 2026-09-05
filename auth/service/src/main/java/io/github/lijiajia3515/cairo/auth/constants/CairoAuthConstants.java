package io.github.lijiajia3515.cairo.auth.constants;

import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TreeNodeTenantAppDepartment;

import java.time.Duration;
import java.util.Comparator;

public class CairoAuthConstants {
	/**
	 * 账号-注销犹豫时间
	 */
	public static final Duration ACCOUNT_LOGOFF_PENDING_TIME = Duration.ofDays(7);

	/**
	 * 应用级用户-注销犹豫时间
	 */

	public static final Duration APP_USER_LOGOFF_PENDING_TIME = Duration.ofDays(3);

	/**
	 * 企业应用级用户-注销犹豫时间
	 */

	public static final Duration TENANT_APP_USER_LOGOFF_PENDING_TIME = Duration.ofDays(3);

	public static final String ROOT_ID = "0";
	public static final String ROOT_PARENT_ID = "-1";
	public static final String TREE_ROOT = "0";


	public static final String ROLE_PREFIX = "role_";

	public static final String APP_ROLE_PREFIX = "app_role_";

	public static final Comparator<TreeNodeTenantAppDepartment> DEPARTMENT_TREE_NODE_COMPARATOR = Comparator.comparing(TreeNodeTenantAppDepartment::getLeftNo).thenComparing(TreeNodeTenantAppDepartment::getDepartmentId);


	public static final Comparator<MenuNode> MENU_NODE_COMPARATOR = Comparator.comparing(MenuNode::getLeftNo).thenComparing(MenuNode::getMenuId);
}
