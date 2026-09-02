package io.github.lijiajia3515.cairo.auth.constants;

import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqQueue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 队列类型
 */
public enum CairoAuthQueue implements CairoRabbitmqQueue {

	// ===== account start =====
	/**
	 * 给新账号发送短信
	 */
	SEND_MESSAGE_BY_CREATED_ACCOUNT("account.send_msg_by_created_account"),

	/**
	 * 给注销账号发送短信
	 */
	SEND_MESSAGE_BY_LOGOFF_ACCOUNT("account.send_msg_by_logoff_account"),

	/**
	 * 给取消注销账号发送短信
	 */
	SEND_MESSAGE_BY_UNLOGOFF_ACCOUNT("account.send_msg_by_unlogoff_account"),
	/**
	 * 给删除账号发送短信
	 */
	SEND_MESSAGE_BY_DELETED_ACCOUNT("account.send_msg_by_deleted_account"),

	/**
	 * 解锁账号
	 */
	UNLOCK_ACCOUNT("account.unlock_account"),

	// ===== account end =====

	// ===== account authorization start =====
	/**
	 * 给新账号发送短信
	 */
	OFFLINE_ACCOUNT_AUTHORIZATION_BY_DELETED_ACCOUNT("account_authorization.offline_account_authorization_by_deleted_account"),

	// ===== account authorization end =====

	// ===== account_login_log start =====
	/**
	 * 删除账号登录日志根据已删除的账号
	 */
	DELETE_ACCOUNT_LOGIN_LOG_BY_DELETED_ACCOUNT("account_login_log.delete_account_login_log_by_deleted_account"),

	/**
	 * 删除账号登录日志根据已删除的客户端
	 */
	DELETE_ACCOUNT_LOGIN_LOG_BY_DELETED_CLIENT("account_login_log.delete_account_login_log_by_deleted_client"),

	/**
	 * 删除部门根据已删除的应用
	 */
	DELETE_ACCOUNT_OAUTH_LOGIN_LOG_BY_DELETED_APP("account_login_log.delete_account_oauth_login_log_by_deleted_app"),
	// ===== account_login_log end =====

	// ===== app start =====
	/**
	 * 注销应用用户根据已删除的账号
	 */
	UNSET_APP_ADMIN_ACCOUNT_BY_DELETED_ACCOUNT("app.unset_app_admin_account_by_deleted_account"),
	// ===== app end =====

	// ===== endpoint start =====

	/**
	 * 删除终端根据已删除的应用
	 */
	DELETE_ENDPOINT_BY_DELETED_APP("endpoint.delete_endpoint_by_deleted_app"),
	// ===== endpoint end =====


	// ===== client start =====

	/**
	 * 修改客户端根据已修改的终端信息
	 */
	MODIFY_CLIENT_BY_MODIFIED_ENDPOINT_INFO("client.modify_client_by_modified_endpoint_info"),
	// ===== client end =====

	// ===== client login log start =====
	/**
	 * 删除客户端登录日志根据已删除的客户端
	 */
	DELETE_CLIENT_LOGIN_LOG_BY_DELETED_CLIENT("client_login_log.delete_login_log_by_deleted_client"),
	// ===== client login log end =====


	// ===== subapp start =====

	/**
	 * 创建默认子应用根据已创建的终端
	 */
	CREATE_SUBAPP_BY_CREATED_ENDPOINT("subapp.create_subapp_by_created_endpoint"),

	/**
	 * 修改子应用根据已修改的终端信息
	 */
	MODIFY_SUBAPP_BY_MODIFIED_ENDPOINT_INFO("subapp.modify_subapp_by_modified_endpoint_info"),


	/**
	 * 根据已删除的终端删除子应用
	 */
	DELETE_SUBAPP_BY_DELETED_ENDPOINT("subapp.delete_subapp_by_deleted_endpoint"),

	// ===== subapp end =====

	// ===== subapp version start =====

	/**
	 * 创建默认版本根据已创建的子应用
	 */
	CREATE_SUBAPP_VERSION_BY_CREATED_SUBAPP("subapp_version.create_subapp_version_by_created_subapp"),

	/**
	 * 修改子应用版本根据已修改的终端信息
	 */
	MODIFY_SUBAPP_VERSION_BY_MODIFIED_ENDPOINT_INFO("subapp_version.modify_subapp_version_by_modified_endpoint_info"),

	/**
	 * 修改子应用版本根据已修改的子应用信息
	 */
	MODIFY_SUBAPP_VERSION_BY_MODIFIED_SUBAPP_INFO("subapp_version.modify_subapp_version_by_modified_subapp_info"),

	/**
	 * 删除单子应用下所有版本
	 */
	DELETE_SUBAPP_VERSION_BY_DELETED_SUBAPP("subapp_version.delete_subapp_version_by_deleted_subapp"),
	// ===== subapp version end =====

	// ===== menu start =====

	/**
	 * 修改菜单根据已修改的终端信息
	 */
	MODIFY_MENU_BY_MODIFIED_ENDPOINT_INFO("menu.modify_menu_by_modified_endpoint_info"),

	/**
	 * 修改子应用版本根据已修改的子应用信息
	 */
	MODIFY_MENU_BY_MODIFIED_SUBAPP_INFO("menu.modify_menu_by_modified_subapp_info"),

	/**
	 * 删除单子应用版本下所有菜单
	 */
	DELETE_MENU_BY_DELETED_SUBAPP_VERSION("menu.delete_menu_by_deleted_subapp_version"),

	/**
	 * 根据子应用版本同步菜单
	 */
	SYNC_MENU_BY_SYNCED_SUBAPP_VERSION("menu.sync_menu_by_synced_subapp_version"),

	// ===== menu end =====

	// ===== permission start =====
	/**
	 * 修改功能权限根据已修改的终端信息
	 */
	MODIFY_PERMISSION_BY_MODIFIED_ENDPOINT_INFO("permission.modify_permission_by_modified_endpoint_info"),
	/**
	 * 修改功能权限根据已修改的子应用信息
	 */
	MODIFY_PERMISSION_BY_MODIFIED_SUBAPP_INFO("permission.modify_permission_by_modified_subapp_info"),
	/**
	 * 删除功能权限
	 */
	DELETE_PERMISSION_BY_DELETED_SUBAPP_VERSION("permission.delete_permission_by_deleted_subapp_version"),
	/**
	 * 同步功能权限根据已同步的客户端的的菜单事件
	 */
	SYNC_PERMISSION_BY_SYNCED_MENU("permission.sync_permission_by_synced_menu"),
	// ===== permission end =====


	// ===== app user start =====

	/**
	 * 发送已创建的企业用户消息
	 */
	SEND_MESSAGE_BY_CREATED_APP_USER("app_user.send_created_app_user_message"),
	/**
	 * 打印应用用户日志队列
	 */
	CREATE_APP_USER_LOG("app_user.create_app_user_log"),
	/**
	 * 创建应用用户根据创建应用
	 */
	CREATE_APP_USER_BY_CREATED_APP("app_user.create_app_user_by_created_app"),
	/**
	 * 创建应用用户根据修改应用
	 */
	CREATE_APP_USER_BY_MODIFIED_APP("app_user.create_app_user_by_modified_app"),
	/**
	 * 删除用户根据已删除的应用
	 */
	DELETE_APP_USER_BY_DELETED_APP("app_user.deleted_app_user_by_deleted_app"),

	/**
	 * 给注销用户发送短信消息
	 */
	SEND_MESSAGE_BY_LOGOFF_APP_USER("app_user.send_logoff_app_user_message"),
	/**
	 * 给取消注销用户发送短信消息
	 */
	SEND_MESSAGE_BY_UNLOGOFF_APP_USER("app_user.send_unlogoff_app_user_message"),
	/**
	 * 根据已注销的用户发送消息
	 */
	SEND_MESSAGE_BY_LOGOFF_SUCCESS_APP_USER("app_user.send_logoff_success_app_user_message"),
	/**
	 * 注销应用用户根据已删除的账号
	 */
	LOGOFF_SUCCESS_APP_USER_BY_DELETED_ACCOUNT("app_user.logoff_success_app_user_by_deleted_account"),

	/**
	 * 根据已创建的企业创建门户应用用户
	 */
	CREATE_PORTAL_APP_USER_BY_CREATED_TENANT("app_user.create_patrol_app_user_by_created_tenant"),
	// ===== app user end =====

	// ===== app role start =====
	/**
	 * 修改应用角色权限根据已修改的终端信息
	 */
	MODIFY_APP_ROLE_PERMISSION_BY_MODIFIED_ENDPOINT_INFO("app_role.modify_role_permission_by_modified_endpoint_info"),

	/**
	 * 删除应用角色
	 */
	DELETE_APP_ROLE("app_role.delete_role_by_deleted_app"),

	/**
	 * 删除应用角色权限
	 */
	DELETE_APP_ROLE_PERMISSION_BY_DELETED_SUBAPP_VERSION("app_role.delete_role_permission_by_deleted_subapp_version"),
	// ===== app  role end =====

	// ===== app department start =====
	/**
	 * 初始化应用部门根据已创建的应用
	 */
	INIT_APP_DEPARTMENT_BY_CREATED_APP("app_department.init_app_department_by_created_app"),

	/**
	 * 删除应用部门根据已删除的应用
	 */
	DELETE_APP_DEPARTMENT_BY_DELETED_APP("app_department.delete_department_by_deleted_app"),
	// ===== app department end =====

	// ===== app user tag start =====

	/**
	 * 删除应用用户标签根据已删除的应用
	 */
	DELETE_APP_USER_TAG_BY_DELETED_APP("app_user_tag.deleted_user_tag_by_deleted_app"),
	// ===== app user tag end =====

	// ===== app endpoint user authorization start =====
	/**
	 * 下线终端用户根据已删除的应用用户
	 */
	OFFLINE_APP_USER_AUTHORIZATION_BY_LOGOFF_SUCCESS_APP_USER("app_user_authorization.offline_app_user_authorization_by_logoff_success_app_user"),

	/**
	 * 下线终端用户根据已删除的应用用户
	 */
	OFFLINE_APP_USER_AUTHORIZATION_BY_DELETED_APP_USER("app_user_authorization.offline_app_user_by_deleted_app_user"),
	// ===== app endpoint user authorization end =====

	// ===== app user login log start =====
	/**
	 * 删除应用用户登录日志根据已删除的客户端
	 */
	DELETE_APP_USER_LOGIN_LOG_BY_DELETED_CLIENT("app_user_login_log.delete_app_user_login_log_by_deleted_client"),

	/**
	 * 删除应用用户登录日志根据已删除的应用用户
	 */
	DELETE_APP_USER_LOGIN_LOG_BY_DELETED_APP_USER("app_user_login_log.delete_app_user_login_log_by_deleted_app_user"),
	/**
	 * 删除应用用户登录日志根据已删除的应用
	 */
	DELETE_APP_USER_LOGIN_LOG_BY_DELETED_APP("app_user_login_log.delete_app_user_login_log_by_deleted_app"),
	// ===== app_user_login_log end =====

	// ===== app user login log (endpoint) start =====
	/**
	 * 修改终端用户登录日志根据已修改的终端信息
	 */
	MODIFY_ENDPOINT_USR_LOGIN_LOG_BY_MODIFIED_ENDPOINT_INFO("app_user_login_log.modify_app_user_login_log_by_modified_endpoint_info"),
	/**
	 * 删除终端应用登录数据-根据已删除的终端
	 */
	DELETE_APP_USER_LOGIN_LOG_DELETED_ENDPOINT("app_user_login_log.delete_app_user_login_log_by_deleted_endpoint"),
	// ===== app user login log (endpoint) end =====


	// ===== tenant start =====
	/**
	 * 已创建企业发送注短信通知
	 */
	SEND_MESSAGE_BY_CREATED_TENANT("tenant.send_created_tenant_message"),
	// ===== tenant end =====

	// ===== tenant app start =====
	/**
	 * 删除企业应用-根据已删除的应用
	 */
	DELETE_TENANT_APP_BY_DELETED_APP("tenant_app.delete_tenant_app_by_deleted_app"),

	/**
	 * 取消企业管理员（根据已删除的账号）
	 */
	UNSET_TENANT_APP_ADMIN_ACCOUNT_BY_DELETED_ACCOUNT("tenant_app.unset_tenant_app_admin_account_by_deleted_account"),
	/**
	 * 已创建企业应用发送注短信通知
	 */
	SEND_MESSAGE_BY_CREATED_TENANT_APP("tenant_app.send_created_tenant_app_message"),
	// ===== tenant app end =====

	// ===== tenant app endpoint start =====
	/**
	 * 根据已删除的终端删除企业终端
	 */
	MODIFY_TENANT_ENDPOINT_BY_MODIFIED_ENDPOINT("tenant_endpoint.delete_tenant_endpoint_by_deleted_endpoint"),

	/**
	 * 根据已删除的企业应用删除企业终端
	 */
	DELETE_TENANT_ENDPOINT_BY_DELETED_TENANT_APP("tenant_endpoint.delete_tenant_endpoint_by_deleted_tenant_app"),

	/**
	 * 根据已删除的终端删除企业终端
	 */
	DELETE_TENANT_ENDPOINT_BY_DELETED_ENDPOINT("tenant_endpoint.delete_tenant_endpoint_by_deleted_endpoint"),

	// ===== tenant app endpoint end =====

	// ===== tenant app subapp start =====

	/**
	 * 根据已删除的企业终端删除企业子应用
	 */
	DELETE_TENANT_SUBAPP_BY_DELETED_TENANT_ENDPOINT("tenant_subapp.delete_tenant_subapp_by_deleted_tenant_endpoint"),

	/**
	 * 根据已删除的子应用删除所有企业子应用
	 */
	DELETE_TENANT_SUBAPP_VERSION_BY_DELETED_SUBAPP("tenant_subapp.delete_tenant_subapp_by_deleted_subapp"),

	/**
	 * 创建企业子应用根据已创建的企业应用
	 */
	CREATE_TENANT_SUBAPP_BY_CREATED_TENANT_APP("tenant_subapp.create_tenant_subapp_by_created_tenant_app"),

	/**
	 * 修改企业子应用根据已修改的子应用信息
	 */
	MODIFY_TENANT_SUBAPP_BY_MODIFIED_SUBAPP_INFO("tenant_subapp.modify_tenant_subapp_by_modified_subapp_info"),

	// ===== tenant app subapp end =====


	// ===== tenant app user start =====
	/**
	 * 打印企业用户日志队列
	 */
	CREATE_TENANT_APP_USER_LOG("tenant_app_user.create_tenant_app_user_log"),

	/**
	 * 创建企业用户根据已创建的企业应用
	 */
	CREATE_TENANT_APP_USER_BY_CREATED_TENANT_APP("tenant_app_user.create_tenant_app_user_by_created_tenant_app"),

	/**
	 * 创建企业用户根据已修改的企业应用
	 */
	CREATE_TENANT_APP_USER_BY_MODIFIED_TENANT_APP("tenant_app_user.create_tenant_app_user_by_modified_tenant_app"),

	/**
	 * 删除企业用户根据已删除的企业应用
	 */
	DELETE_TENANT_APP_USER_BY_DELETED_TENANT_APP("tenant_app_user.deleted_tenant_app_user_by_deleted_tenant_app"),

	/**
	 * 注销完成企业用户根据已删除的账号
	 */
	LOGOFF_SUCCESS_TENANT_APP_USER_BY_DELETED_ACCOUNT("tenant_app_user.logoff_success_tenant_app_user_by_deleted_account"),

	/**
	 * 发送已创建的企业用户消息
	 */
	SEND_MESSAGE_BY_CREATED_TENANT_APP_USER("tenant_app_user.send_created_tenant_app_user_message"),

	/**
	 * 给注销企业用户发送消息
	 */
	SEND_MESSAGE_BY_LOGOFF_TENANT_APP_USER("tenant_app_user.send_logoff_tenant_app_user_message"),

	/**
	 * 给取消注销企业用户发送消息
	 */
	SEND_MESSAGE_BY_UNLOGOFF_TENANT_APP_USER("tenant_app_user.send_unlogoff_tenant_app_user_message"),

	/**
	 * 给已注销企业用户发送消息
	 */
	SEND_MESSAGE_BY_LOGOFF_SUCCESS_TENANT_APP_USER("tenant_app_user.send_logoff_success_tenant_app_user_message"),

	/**
	 * 创建企业用户根据已创建的企业用户模板
	 */
	CREATE_TENANT_APP_USER_BY_CREATED_TENANT_APP_TEMPLATE("tenant_app_user.create_tenant_app_user_by_created_tenant_app_user_template"),
	// ===== tenant app user end =====


	// ===== tenant app role start =====
	/**
	 * 修改角色权限根据已修改的终端信息
	 */
	MODIFY_TENANT_ROLE_PERMISSION_BY_MODIFIED_ENDPOINT_INFO("tenant_role.modify_tenant_role_permission_by_modified_endpoint_info"),
	/**
	 * 删除功能权限
	 */
	DELETE_TENANT_ROLE_PERMISSION_BY_DELETED_TENANT_SUBAPP("tenant_role.delete_tenant_role_permission_by_deleted_tenant_subapp"),

	/**
	 * 删除角色根据已删除的企业应用
	 */
	DELETE_TENANT_ROLE_BY_DELETED_TENANT_APP("tenant_app_role.delete_tenant_app_role_by_deleted_tenant_app"),

	/**
	 * 创建企业角色根据已创建的企业角色模板
	 */
	CREATE_TENANT_APP_ROLE_BY_CREATED_TENANT_ROLE_TEMPLATE("tenant_app_role.create_tenant_app_role_by_created_tenant_role_template"),
	// ===== tenant app role end =====

	// ===== tenant app department start =====
	/**
	 * 初始化企业应用部门根据已创建的企业应用
	 */
	INIT_TENANT_APP_DEPARTMENT_BY_CREATED_TENANT_APP("tenant_app_department.init_tenant_app_department_by_created_tenant_app"),
	/**
	 * 删除部门根据已删除的应用
	 */
	DELETE_TENANT_DEPARTMENT_BY_DELETED_TENANT_APP("tenant_app_department.delete_tenant_app_department_by_deleted_tenant_app"),

	/**
	 * 创建企业部门根据已创建的企业部门模板
	 */
	CREATE_TENANT_APP_DEPARTMENT_BY_CREATED_TENANT_DEPARTMENT_TEMPLATE("tenant_app_department.create_tenant_app_department_by_created_tenant_department_template"),
	// ===== tenant app department end =====

	// ===== tenant app user tag start =====
	/**
	 * 删除用户标签根据已删除的应用
	 */
	DELETE_TENANT_APP_USER_TAG_BY_DELETED_TENANT_APP("tenant_app_user_tag.deleted_tenant_app_user_tag_by_deleted_app"),
	// ===== tenant app user tag end =====

	// ===== tenant app user login log start =====
	/**
	 * 删除企业应用用户登录日志根据已删除的客户端
	 */
	DELETE_TENANT_APP_USER_LOGIN_LOG_BY_DELETED_CLIENT("tenant_app_user_login_log.delete_tenant_app_user_login_log_by_deleted_client"),

	/**
	 * 删除企业应用用户登录日志根据已删除的用户
	 */
	DELETE_TENANT_APP_USER_LOGIN_LOG_BY_DELETED_TENANT_APP_USER("tenant_app_user_login_log.delete_tenant_app_user_login_log_by_deleted_tenant_app_user"),
	/**
	 * 删除企业应用用户登录日志根据已删除的应用
	 */
	DELETE_TENANT_APP_USER_LOGIN_LOG_BY_DELETED_TENANT_APP("tenant_app_user_login_log.delete_tenant_app_user_login_log_by_deleted_tenant_app"),
	// ===== tenant app user login log end =====

	// ===== tenant app user login log (endpoint) start =====
	/**
	 * 修改终端用户登录日志根据已修改的终端信息
	 */
	MODIFY_TENANT_APP_USER_LOGIN_LOG_BY_MODIFIED_ENDPOINT_INFO("tenant_app_user_login_log.modify_tenant_app_user_login_log_by_modified_endpoint_info"),
	/**
	 * 删除终端应用登录数据-根据已删除的终端
	 */
	DELETE_TENANT_APP_USER_LOGIN_LOG_DELETED_TENANT_ENDPOINT("tenant_app_user_login_log.delete_tenant_app_user_login_log_by_deleted_tenant_endpoint"),
	// ===== tenant app user login log (endpoint) end =====

	// ===== open biz log start =====
	/**
	 * 根据已删除的用户删除用户业务日志
	 */
	DELETE_OPEN_BIZ_LOG_BY_DELETED_APP("open_biz_log.delete_open_biz_log_by_deleted_app"),
	// ===== open biz log end =====

	// ===== client biz log start =====
	/**
	 * 根据已删除的用户删除用户业务日志
	 */
	DELETE_CLIENT_BIZ_LOG_BY_DELETED_CLIENT("client_biz_log.delete_client_biz_log_by_deleted_client"),
	// ===== client biz log end =====

	// ===== account biz log start =====
	/**
	 * 根据已删除的账号删除账号业务日志
	 */
	DELETE_ACCOUNT_BIZ_LOG_BY_DELETED_ACCOUNT("account_biz_log.delete_account_biz_log_by_deleted_account"),

	/**
	 * 根据已删除的客户端删除账号业务日志
	 */
	DELETE_ACCOUNT_BIZ_LOG_BY_DELETED_CLIENT("account_biz_log.delete_account_biz_log_by_deleted_client"),

	/**
	 * 根据已删除的应用删除账号业务日志
	 */
	DELETE_ACCOUNT_BIZ_LOG_BY_DELETED_APP("account_biz_log.delete_account_biz_log_by_deleted_app"),
	// ===== account biz log end =====

	// ===== app biz log start =====


	// ===== app biz log end =====

	// ===== app endpoint biz log start =====
	/**
	 * 修改终端用户日志根据已修改的终端信息
	 */
	MODIFY_APP_BIZ_LOG_BY_MODIFIED_ENDPOINT_INFO("app_biz_log.modify_app_biz_log_by_modified_endpoint_info"),

	/**
	 * 根据已删除的应用删除终端用户业务日志
	 */
	DELETE_APP_BIZ_LOG_BY_DELETED_ENDPOINT("app_biz_log.delete_app_biz_log_by_deleted_endpoint"),

	/**
	 * 根据已删除的用户删除终端用户业务日志
	 */
	DELETE_APP_BIZ_LOG_BY_DELETED_CLIENT("app_biz_log.delete_app_biz_log_by_deleted_client"),


	/**
	 * 根据已删除的用户删除终端用户业务日志
	 */
	DELETE_APP_BIZ_LOG_BY_DELETED_APP_USER("app_biz_log.delete_app_biz_log_by_deleted_app_user"),
	// ===== app endpoint biz log end =====

	// ===== app subapp biz log start =====
	/**
	 * 修改子应用业务日志根据已修改的终端信息
	 */
	MODIFY_SUBAPP_BIZ_LOG_BY_MODIFIED_SUBAPP_INFO("subapp_biz_log.modify_subapp_biz_log_by_modified_subapp_info"),

	/**
	 * 根据已删除的子应用删除子应用业务日志
	 */
	DELETE_SUBAPP_BIZ_LOG_BY_DELETED_SUBAPP("subapp_biz_log.delete_subapp_biz_log_by_deleted_subapp"),

	/**
	 * 根据已删除的用户删除终端用户业务日志
	 */
	DELETE_SUBAPP_BIZ_LOG_BY_DELETED_APP_USER("subapp_biz_log.delete_subapp_biz_log_by_deleted_app_user"),
	// ===== app subapp biz log end =====

	// ===== tenant app biz log start =====


	// ===== tenant app biz log end =====

	// ===== tenant app endpoint biz log start =====
	/**
	 * 修改终端用户日志根据已修改的终端信息
	 */
	MODIFY_TENANT_APP_BIZ_LOG_BY_MODIFIED_ENDPOINT_INFO("tenant_app_biz_log.modify_tenant_app_biz_log_by_modified_endpoint_info"),

	/**
	 * 根据已删除的用户删除终端用户业务日志
	 */
	DELETE_TENANT_APP_BIZ_LOG_BY_DELETED_TENANT_APP_USER("tenant_app_biz_log.delete_tenant_app_biz_log_by_deleted_tenant_app_user"),

	/**
	 * 根据已删除的用户删除终端用户业务日志
	 */
	DELETE_TENANT_APP_BIZ_LOG_BY_DELETED_CLIENT("tenant_app_biz_log.delete_tenant_app_biz_log_by_deleted_client"),


	/**
	 * 根据已删除的应用删除终端用户业务日志
	 */
	DELETE_TENANT_APP_BIZ_LOG_BY_DELETED_ENDPOINT("tenant_endpoint_biz_log.delete_tenant_endpoint_biz_log_by_deleted_endpoint"),
	// ===== tenant app endpoint biz log end =====

	// ===== tenant app subapp biz log start =====
	/**
	 * 修改终端用户日志根据已修改的终端信息
	 */
	MODIFY_TENANT_SUBAPP_BIZ_LOG_BY_MODIFIED_SUBAPP_INFO("tenant_subapp_biz_log.modify_tenant_subapp_biz_log_by_modified_subapp_info"),

	/**
	 * 根据已删除的用户删除子应用业务日志
	 */
	DELETE_TENANT_SUBAPP_BIZ_LOG_BY_DELETED_TENANT_APP_USER("tenant_subapp_biz_log.delete_tenant_subapp_biz_log_by_deleted_tenant_app_user"),

	/**
	 * 根据已删除的企业子应用删除子应用业务日志
	 */
	DELETE_TENANT_SUBAPP_BIZ_LOG_BY_DELETED_TENANT_SUBAPP("tenant_subapp_biz_log.delete_tenant_subapp_biz_log_by_deleted_tenant_subapp"),
	// ===== tenant app subapp biz log end =====


	// ===== sms_msg start =====
	/**
	 * 根据手机号发送消息
	 */
	SEND_MESSAGE_BY_PHONE_NUMBER("sms_msg.send_msg_by_phone_number"),

	DELETE_SMS_MSG_BY_DELETED_SMS_TEMPLATE("sms_msg.delete_sms_msg_by_deleted_sms_template"),
	// ===== sms_msg end =====

	// ===== file start =====
	/**
	 * 创建租户存储通
	 */
	CREATE_TENANT_BUCKET("file.create_tenant_bucket"),
	// ===== file end =====

	// ===== dict start =====
	/**
	 * 同步系统级字典
	 */
	SYNC_BIZ_DICT_BY_SYS_DICT("sync_biz_dict_by_sys_dict"),

	/**
	 * 删除业务级字典
	 */
	DELETE_BIZ_DICT("biz_dict.delete_biz_dict_by_deleted_sys_dict"),

	/**
	 * 删除系统级字典根据已删除应用
	 */
	DELETE_SYS_DICT_BY_DELETE_APP("delete_sys_dict_by_delete_app"),

	/**
	 * 复制系统级字典根据字典id
	 */
	COPY_SYS_DICT_BY_DICT("copy_sys_dict_by_dict_id"),

	/**
	 * 同步业务级字典根据创建企业应用
	 */
	SYNC_BIZ_DICT_BY_CREATED_TENANT_APP("biz_dict.sync_biz_dict_by_created_tenant_app"),

	/**
	 * 删除业务级字典根据删除企业应用
	 */
	DELETE_BIZ_DICT_BY_DELETED_TENANT_APP("biz_dict.delete_biz_dict_by_deleted_tenant_app"),
	// ===== dict end =====

	// ===== app_release start =====
	/**
	 * 应用发行版根据已修改的终端
	 */
	MODIFY_APP_RELEASE_BY_MODIFIED_ENDPOINT_INFO("modify_app_release_by_modified_endpoint_info"),

	/**
	 * 应用发行版本删除根据已删除终端
	 */
	DELETE_APP_RELEASE_BY_DELETED_ENDPOINT("delete_app_release_by_deleted_endpoint"),
	// ===== app_release end =====
	;
	/**
	 * 队列名称
	 */
	private final String name;

	CairoAuthQueue(@Valid @NotNull String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
