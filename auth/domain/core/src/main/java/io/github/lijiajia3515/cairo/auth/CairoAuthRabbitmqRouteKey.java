package io.github.lijiajia3515.cairo.auth;

import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqExchange;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqRouteKey;

/**
 * 路由事件
 */
public enum CairoAuthRabbitmqRouteKey implements CairoRabbitmqRouteKey {

	/**
	 * 已创建账号消息
	 */
	CREATED_ACCOUNT("open_scope.account.created_account"),

	/**
	 * 已修改账号消息
	 */
	MODIFIED_ACCOUNT_PASSWORD("open_scope.account.modified_account_password"),

	/**
	 * 注销的账号消息
	 */
	LOGOFF_ACCOUNT("open_scope.account.logoff_account"),

	/**
	 * 取消注销的账号消息
	 */
	UNLOGOFF_ACCOUNT("open_scope.account.unlogoff_account"),

	/**
	 * 解锁账号消息
	 */
	UNLOCK_ACCOUNT("open_scope.account.unlock_locked"),

	/**
	 * 已删除账号消息
	 */
	DELETED_ACCOUNT("open_scope.account.deleted_account"),


	// =========== app start ===========
	/**
	 * app创建完成
	 */
	CREATED_APP("open_scope.app.created_app"),

	/**
	 * 修改app信息
	 */
	MODIFIED_APP_INFO("app_scope.$appId.app.modified_app_info"),

	/**
	 * app状态修改完成
	 */
	MODIFIED_APP_STATUS("app_scope.$appId.app.modified_app_status"),

	/**
	 * app删除完成
	 */
	DELETED_APP("app_scope.$appId.app.deleted_app"),

	// =========== app end ===========

	// =========== app endpoint start ===========

	/**
	 * app endpoint 创建完成
	 */
	CREATED_ENDPOINT("app_scope.$appId.endpoint.created_endpoint"),


	/**
	 * app endpoint 信息修改完成
	 */
	MODIFIED_ENDPOINT_INFO("app_scope.$appId.endpoint.modified_endpoint_info"),

	/**
	 * app endpoint 状态修改完成
	 */
	MODIFIED_ENDPOINT_STATUS("app_scope.$appId.endpoint.modified_endpoint_status"),

	/**
	 * app endpoint 删除完成
	 */
	DELETED_ENDPOINT("app_scope.$appId.endpoint.deleted_endpoint"),
	// =========== app endpoint end ===========

	// =========== subapp start ===========

	/**
	 * 创建子应用完成
	 */
	CREATED_SUBAPP("app_scope.$appId.subapp.created_subapp"),


	/**
	 * 修改子应用信息完成
	 */
	MODIFIED_SUBAPP_INFO("app_scope.$appId.subapp.modified_subapp_info"),

	/**
	 * 修改子应用状态完成
	 */
	MODIFIED_SUBAPP_STATUS("app_scope.$appId.subapp.modified_subapp_status"),

	/**
	 * 删除子应用完成
	 */
	DELETED_SUBAPP("app_scope.$appId.subapp.deleted_subapp"),

	/**
	 * 删除子应用版本完成
	 */
	DELETED_SUBAPP_VERSION("app_scope.$appId.subapp_version.deleted_subapp_version"),


	/**
	 * 同步子应用版本完成
	 */
	SYNCED_SUBAPP_VERSION("app_scope.$appId.subapp_version.synced_subapp_version"),

	// =========== subapp end ===========

	// =========== client start ===========
	/**
	 * app endpoint 创建完成
	 */
	CREATED_CLIENT("app_scope.$appId.client.created_client"),

	/**
	 * client 信息修改完成
	 */
	MODIFIED_CLIENT_INFO("app_scope.$appId.client.modified_client_info"),

	/**
	 * client 状态修改完成
	 */
	MODIFIED_CLIENT_STATUS("app_scope.$appId.client.modified_client_status"),

	/**
	 * client 状态修改完成
	 */
	MODIFIED_CLIENT_SECRET("app_scope.$appId.client.modified_client_secret"),

	/**
	 * client 删除完成
	 */
	DELETED_CLIENT("app_scope.$appId.client.deleted_client"),

	// =========== client end ===========

	// =========== menu start ===========
	/**
	 * 已同步完成的菜单事件
	 */
	SYNCED_MENU("app_scope.$appId.menu.synced_menu"),
	// =========== menu end ===========

	// =========== app user start ===========
	/**
	 * 已创建的应用用户
	 */
	CREATED_APP_USER("app_scope.$appId.user.created_app_user"),
	/**
	 * 注销应用用户
	 */
	LOGOFF_APP_USER("app_scope.$appId.app_user.logoff_user"),
	/**
	 * 注销应用用户
	 */
	UNLOGOFF_APP_USER("app_scope.$appId.app_user.unlogoff_user"),
	/**
	 * 已注销应用用户
	 */
	LOGOFF_SUCCESS_APP_USER("app_scope.$appId.app_user.logoff_success_user"),

	/**
	 * 应用用户删除完成
	 */
	DELETED_APP_USER("app_scope.$appId.app_user.deleted_user"),
	// =========== app user end ===========

	// =========== app user tag start ===========

	/**
	 * 应用用户标签创建完成
	 */
	CREATED_APP_USER_TAG("app_scope.$appId.app_user_tag.created_app_user_tag"),
	// =========== app user tag end ===========


	// =========== tenant start ===========
	/**
	 * 企业创建完成
	 */
	CREATED_TENANT("tenant_scope.$tenantId.tenant.created_tenant"),

	/**
	 * 修改企业信息
	 */
	MODIFIED_TENANT_INFO("tenant_scope.$tenantId.tenant.modified_tenant_info"),

	/**
	 * 修改企业信息
	 */
	MODIFIED_TENANT_STATUS("tenant_scope.$tenantId.tenant.modified_tenant_status"),

	/**
	 * 已删除企业
	 */
	DELETED_TENANT("tenant_scope.$tenantId.tenant.deleted_tenant_status"),
	// =========== tenant end ===========

	// =========== tenant app start ===========
	/**
	 * 创建企业应用完成
	 */
	CREATED_TENANT_APP("tenant_app_scope.$tenantId.$appId.tenant_app.created_tenant_app"),

	/**
	 * 修改企业应用信息
	 */
	MODIFIED_TENANT_APP_INFO("tenant_app_scope.$tenantId.$appId.tenant_app.modified_tenant_app_info"),

	/**
	 * 修改企业应用信息
	 */
	MODIFIED_TENANT_APP_STATUS("tenant_app_scope.$tenantId.$appId.tenant_app.modified_tenant_app_status"),

	/**
	 * 删除企业应用完成
	 */
	DELETED_TENANT_APP("tenant_app_scope.$tenantId.$appId.tenant_app.deleted_tenant_app"),
	// =========== tenant app end ===========

	// =========== tenant app endpoint start ===========
	/**
	 * 创建企业终端完成
	 */
	CREATED_TENANT_ENDPOINT("tenant_app_scope.$tenantId.$appId.tenant_endpoint.created_tenant_endpoint"),

	/**
	 * 修改企业终端状态
	 */
	MODIFIED_TENANT_ENDPOINT_STATUS("tenant_app_scope.$tenantId.$appId.tenant_endpoint.modified_tenant_endpoint_status"),

	/**
	 * 删除企业终端
	 */
	DELETED_TENANT_ENDPOINT("tenant_app_scope.$tenantId.$appId.tenant_endpoint.deleted_tenant_endpoint"),
	// =========== tenant app endpoint end ===========

	// =========== tenant app subapp start ===========
	/**
	 * 创建企业子应用完成
	 */
	CREATED_TENANT_SUBAPP("tenant_app_scope.$tenantId.$appId.tenant_subapp.created_tenant_subapp"),

	/**
	 * 修改企业子应用信息完成
	 */
	MODIFIED_TENANT_SUBAPP_INFO("tenant_app_scope.$tenantId.$appId.tenant_subapp.modified_tenant_subapp_info"),

	/**
	 * 修改企业子应用状态完成
	 */
	MODIFIED_TENANT_SUBAPP_STATUS("tenant_app_scope.$tenantId.$appId.tenant_subapp.modified_tenant_subapp_status"),

	/**
	 * 删除企业子应用完成
	 */
	DELETED_TENANT_SUBAPP("tenant_app_scope.$tenantId.$appId.tenant_subapp.deleted_tenant_subapp"),
	// =========== tenant app subapp end ===========



	// =========== tenant app user start ===========
	/**
	 * 已创建的用户
	 */
	CREATED_TENANT_APP_USER("tenant_app_scope.$tenantId.$appId.tenant_app_user.created_tenant_app_user"),

	/**
	 * 注销用户
	 */
	LOGOFF_TENANT_APP_USER("tenant_app_scope.$tenantId.$appId.tenant_app_user.logoff_tenant_app_user"),

	/**
	 * 注销用户
	 */
	UNLOGOFF_TENANT_APP_USER("tenant_app_scope.$tenantId.$appId.tenant_app_user.unlogoff_tenant_app_user"),

	/**
	 * 已注销企业用户
	 */
	LOGOFF_SUCCESS_TENANT_APP_USER("tenant_app_scope.$tenantId.$appId.tenant_app_user.logoff_success_tenant_app_user"),

	/**
	 * 用户删除完成
	 */
	DELETED_TENANT_APP_USER("tenant_app_scope.$tenantId.$appId.tenant_app_user.deleted_tenant_app_user"),

	// =========== tenant app user end ===========

	// =========== tenant app user tag start ===========
	/**
	 * 用户标签创建完成
	 */
	CREATED_TENANT_APP_USER_TAG("tenant_app_scope.$tenantId.$appId.tenant_app_user_tag.created_tenant_app_user_tag"),

	/**
	 * 企业用户标签删除完成
	 */
	DELETED_TENANT_APP_USER_TAG("tenant_app_scope.$tenantId.$appId.tenant_app_user_tag.deleted_tenant_app_user_tag"),

	/**
	 * 企业用户标签信息修改完成
	 */
	MODIFIED_TENANT_APP_USER_TAG_INFO("tenant_app_scope.$tenantId.$appId.tenant_app_user_tag.modified_tenant_app_user_tag_info"),

	/**
	 * 企业用户标签状态修改完成
	 */
	MODIFIED_TENANT_APP_USER_TAG_STATUS("tenant_app_scope.$tenantId.$appId.tenant_app_user_tag.modified_tenant_app_user_tag_status"),
	// =========== tenant app user tag end ===========

	// =========== sms start ===========
	/**
	 * 发送短信消息 事件
	 */
	SEND_MESSAGE_BY_PHONE_NUMBER("app_scope.$appId.sms_msg.send_msg_by_phone_number"),

	/**
	 * 已删除短信模板 事件
	 */
	DELETED_SMS_TEMPLATE("app_scope.$appId.sms_template.deleted_sms_template"),
	// =========== sms end ===========

	// =========== notification message  start ===========
	/**
	 * 发送短信消息 事件
	 */
	SEND_NOTIFY_BY_XX("app_scope.$appId.notify.send_msg_by_xx"),

	/**
	 * 已删除通知消息模板 事件
	 */
	DELETED_NOTIFY_TEMPLATE("app_scope.$appId.notify.deleted_notify_template"),
	// =========== notification message end ===========

	/**
	 * 同步系统字典消息
	 */
	SYNC_SYS_DICT("tenant_app_scope.$tenantId.$appId.sync_sys_dict"),

	/**
	 * 删除系统字典消息
	 */
	DELETED_SYS_DICT("tenant_app_scope.$tenantId.$appId.deleted_sys_dict"),

	/**
	 * 复制系统字典消息
	 */
	COPY_SYS_DICT("app_scope.$appId.sys_dict.copy_sys_dict"),

	;

	/**
	 * 路由名称
	 */
	private final String name;


	CairoAuthRabbitmqRouteKey(String name) {
		this.name = name;
	}


	@Override
	public CairoRabbitmqExchange getExchange() {
		return CairoAuthRabbitmqExchange.AUTH;
	}

	@Override
	public String getName() {
		return name;
	}
}
