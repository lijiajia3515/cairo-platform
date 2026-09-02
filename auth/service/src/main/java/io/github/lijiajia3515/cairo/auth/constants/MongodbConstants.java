package io.github.lijiajia3515.cairo.auth.constants;

/**
 * Mongodb 集合常量
 * <p>
 * 常量值即 MongoDB 集合名，严禁改动（数据兼容性）；
 * {@link DeletedCollection} 为软删除集合，部分集合名与主集合不一致，同样严禁改动。
 */
public class MongodbConstants {

	/**
	 * 主集合
	 */
	public static class Collection {
		private static final String PREFIX = "auth";

		// ==================== 通用 ====================

		public static final String SERIAL = collection("serial");

		// ==================== 账号 ====================

		public static final String ACCOUNT = collection("account");
		public static final String ACCOUNT_SNS = collection("account_sns");
		public static final String ACCOUNT_PASSWORD = collection("account_password");
		public static final String ACCOUNT_LOGIN_LOG = collection("account_login_log");
		public static final String ACCOUNT_AUTHORIZATION = collection("account_authorization");

		// ==================== OAuth2 客户端 ====================

		public static final String OAUTH2_AUTHORIZATION = collection("oauth2_authorization");
		public static final String CLIENT = collection("client");
		public static final String CLIENT_LOGIN_LOG = collection("client_login_log");

		// ==================== 菜单与权限 ====================

		public static final String MENU = collection("menu");
		public static final String PERMISSION = collection("permission");

		// ==================== 应用 ====================

		public static final String APP = collection("app");
		public static final String ENDPOINT = collection("endpoint");
		public static final String SUBAPP = collection("subapp");
		public static final String SUBAPP_VERSION = collection("subapp_version");
		public static final String APP_RELEASE = collection("app_release");

		public static final String APP_ROLE = collection("app_role");
		public static final String APP_ROLE_PERMISSION = collection("app_role_permission");
		public static final String APP_DEPARTMENT = collection("app_department");

		public static final String APP_USER = collection("app_user");
		public static final String APP_USER_TAG = collection("app_user_tag");
		public static final String APP_USER_AUTHORIZATION = collection("app_user_authorization");
		public static final String APP_USER_LOGIN_LOG = collection("app_user_login_log");

		// ==================== 企业 ====================

		public static final String TENANT = collection("tenant");
		public static final String TENANT_APP = collection("tenant_app");
		public static final String TENANT_ENDPOINT = collection("tenant_endpoint");
		public static final String TENANT_SUBAPP = collection("tenant_subapp");

		public static final String TENANT_APP_USER = collection("tenant_app_user");
		public static final String TENANT_APP_ROLE = collection("tenant_app_role");
		public static final String TENANT_APP_ROLE_PERMISSION = collection("tenant_app_role_permission");
		public static final String TENANT_APP_DEPARTMENT = collection("tenant_app_department");
		public static final String TENANT_APP_USER_TAG = collection("tenant_app_user_tag");
		public static final String TENANT_APP_USER_SNS = collection("tenant_app_user_sns");
		public static final String TENANT_APP_USER_AUTHORIZATION = collection("tenant_app_user_authorization");
		public static final String TENANT_APP_USER_LOGIN_LOG = collection("tenant_app_user_login_log");

		public static final String TENANT_APP_DEPARTMENT_TEMPLATE = collection("tenant_app_department_template");
		public static final String TENANT_APP_ROLE_TEMPLATE = collection("tenant_app_role_template");
		public static final String TENANT_APP_ROLE_TEMPLATE_PERMISSION = collection("tenant_app_role_template_permission");
		public static final String TENANT_APP_USER_TEMPLATE = collection("tenant_app_user_template");

		// ==================== 三方认证 ====================

		public static final String SNS_PROVIDER = collection("sns_provider");
		public static final String SNS_TOKEN = collection("sns_token");

		// ==================== 字典 ====================

		public static final String SYS_DICT = collection("sys_dict");
		public static final String SYS_DICT_ITEM = collection("sys_dict_item");
		public static final String BIZ_DICT = collection("biz_dict");
		public static final String BIZ_DICT_ITEM = collection("biz_dict_item");

		// ==================== 行政区划 ====================

		public static final String AREA = collection("area");

		// ==================== 文件 ====================

		public static final String OFFICE_FILE = collection("office_file");
		public static final String OFFICE_FILE_VERSION = collection("office_file_version");

		// ==================== 短信 ====================

		public static final String SMS_TEMPLATE = collection("sms_template");
		public static final String SMS_TEMPLATE_ARG = collection("sms_template_arg");
		public static final String SMS_MSG = collection("sms_msg");

		// ==================== 微信公众号 ====================

		public static final String WXMP_PROVIDER = collection("wxmp_provider");
		public static final String WXMP_TEMPLATE_MSG = collection("wxmp_template_msg");
		public static final String WXMP_TEMPLATE_MSG_ARGS = collection("wxmp_template_msg_args");
		public static final String WXMP_TEMPLATE_MSG_RECORD = collection("wxmp_template_msg_record");
		public static final String WXMP_APP_USER = collection("wxmp_app_user");
		public static final String TENANT_APP_USER_WXMP = collection("wxmp_tenant_app_user");

		// ==================== 通知消息 ====================

		public static final String NOTIFY_CATEGORY = collection("notify_category");
		public static final String NOTIFY_TEMPLATE = collection("notify_template");
		public static final String NOTIFY_TEMPLATE_ARGS = collection("notify_template_args");
		public static final String NOTIFY_RECORD_APP = collection("notify_record_app");
		public static final String NOTIFY_RECORD_TENANT_APP = collection("notify_record_tenant_app");

		// ==================== 短链 ====================

		public static final String LINK = collection("link");

		// ==================== 业务日志 ====================

		public static final String BIZ_LOG_OPEN = collection("biz_log_open");
		public static final String BIZ_LOG_CLIENT = collection("biz_log_client");
		public static final String BIZ_LOG_ACCOUNT = collection("biz_log_account");
		public static final String BIZ_LOG_APP = collection("biz_log_app");
		public static final String BIZ_LOG_SUBAPP = collection("biz_log_subapp");
		public static final String BIZ_LOG_TENANT_APP = collection("biz_log_tenant_app");
		public static final String BIZ_LOG_TENANT_SUBAPP = collection("biz_log_tenant_subapp");

		private static String collection(String collection) {
			return PREFIX.concat("_").concat(collection);
		}
	}

	/**
	 * 软删除集合
	 */
	public static class DeletedCollection {
		private static final String PREFIX = "auth_deleted";

		// ==================== 通用 ====================

		public static final String SERIAL = collection("serial");

		// ==================== 账号 ====================

		public static final String ACCOUNT = collection("account");
		public static final String ACCOUNT_SNS = collection("account_sns");
		public static final String ACCOUNT_PASSWORD = collection("account_password");
		public static final String ACCOUNT_LOGIN_LOG = collection("account_login_log");
		public static final String ACCOUNT_AUTHORIZATION = collection("account_authorization");

		// ==================== OAuth2 客户端 ====================

		public static final String OAUTH2_AUTHORIZATION = collection("oauth2_authorization");
		public static final String CLIENT = collection("client");
		public static final String CLIENT_LOGIN_LOG = collection("client_login_log");

		// ==================== 菜单与权限 ====================

		public static final String MENU = collection("menu");
		public static final String PERMISSION = collection("permission");

		// ==================== 应用 ====================

		public static final String APP = collection("app");
		public static final String ENDPOINT = collection("endpoint");
		public static final String SUBAPP = collection("subapp");
		public static final String SUBAPP_VERSION = collection("subapp_version");
		public static final String APP_RELEASE = collection("app_release");

		public static final String APP_ROLE = collection("app_role");
		public static final String APP_ROLE_PERMISSION = collection("app_role_permission");
		public static final String APP_DEPARTMENT = collection("app_department");

		public static final String APP_USER = collection("app_user");
		public static final String APP_USER_TAG = collection("app_user_tag");
		public static final String APP_USER_AUTHORIZATION = collection("app_user_authorization");
		public static final String APP_USER_LOGIN_LOG = collection("app_user_login_log");

		// ==================== 企业 ====================
		// 注意：TENANT_APP_ROLE / TENANT_APP_ROLE_PERMISSION 的软删除集合名与主集合不一致，严禁“修正”

		public static final String TENANT = collection("tenant");
		public static final String TENANT_APP = collection("tenant_app");
		public static final String TENANT_ENDPOINT = collection("tenant_endpoint");
		public static final String TENANT_SUBAPP = collection("tenant_subapp");

		public static final String TENANT_APP_USER = collection("tenant_app_user");
		public static final String TENANT_APP_ROLE = collection("tenant_role");
		public static final String TENANT_APP_ROLE_PERMISSION = collection("role_permission");
		public static final String TENANT_APP_DEPARTMENT = collection("tenant_app_department");
		public static final String TENANT_APP_USER_TAG = collection("tenant_app_user_tag");
		public static final String TENANT_APP_USER_SNS = collection("tenant_app_user_sns");
		public static final String TENANT_APP_USER_AUTHORIZATION = collection("tenant_app_user_authorization");
		public static final String TENANT_APP_USER_LOGIN_LOG = collection("tenant_app_user_login_log");

		public static final String TENANT_APP_DEPARTMENT_TEMPLATE = collection("tenant_app_department_template");
		public static final String TENANT_APP_ROLE_TEMPLATE = collection("tenant_app_role_template");
		public static final String TENANT_APP_ROLE_TEMPLATE_PERMISSION = collection("tenant_app_role_template_permission");
		public static final String TENANT_APP_USER_TEMPLATE = collection("tenant_app_user_template");

		// ==================== 三方认证 ====================

		public static final String SNS_PROVIDER = collection("sns_provider");

		// ==================== 字典 ====================

		public static final String SYS_DICT = collection("sys_dict");
		public static final String SYS_DICT_ITEM = collection("sys_dict_item");
		public static final String BIZ_DICT = collection("biz_dict");
		public static final String BIZ_DICT_ITEM = collection("biz_dict_item");

		// ==================== 行政区划 ====================

		public static final String AREA = collection("area");

		// ==================== 文件（软删除集合沿用 wps_file 历史名，严禁“修正”） ====================

		public static final String WPS_FILE = collection("wps_file");
		public static final String WPS_FILE_VERSION = collection("wps_file_version");

		// ==================== 短信 ====================

		public static final String SMS_TEMPLATE = collection("sms_template");
		public static final String SMS_TEMPLATE_ARG = collection("sms_template_arg");
		public static final String SMS_MSG = collection("sms_msg");

		// ==================== 微信公众号 ====================

		public static final String WXMP_PROVIDER = collection("wxmp_provider");
		public static final String WXMP_TEMPLATE_MSG = collection("wxmp_template_msg");
		public static final String WXMP_TEMPLATE_MSG_ARGS = collection("wxmp_template_msg_args");
		public static final String WXMP_TEMPLATE_MSG_RECORD = collection("wxmp_template_msg_record");
		public static final String WXMP_APP_USER = collection("wxmp_app_user");
		public static final String TENANT_APP_USER_WXMP = collection("wxmp_tenant_app_user");

		// ==================== 通知消息 ====================

		public static final String NOTIFY_CATEGORY = collection("notify_category");
		public static final String NOTIFY_TEMPLATE = collection("notify_template");
		public static final String NOTIFY_TEMPLATE_ARGS = collection("notify_template_args");
		public static final String NOTIFY_RECORD_APP = collection("notify_record_app");
		public static final String NOTIFY_RECORD_TENANT_APP = collection("notify_record_tenant_app");

		// ==================== 短链 ====================

		public static final String LINK = collection("link");

		// ==================== 业务日志 ====================

		public static final String BIZ_LOG_OPEN = collection("biz_log_open");
		public static final String BIZ_LOG_CLIENT = collection("biz_log_client");
		public static final String BIZ_LOG_ACCOUNT = collection("biz_log_account");
		public static final String BIZ_LOG_APP = collection("biz_log_app");
		public static final String BIZ_LOG_SUBAPP = collection("biz_log_subapp");
		public static final String BIZ_LOG_TENANT_APP = collection("biz_log_tenant_app");
		public static final String BIZ_LOG_TENANT_SUBAPP = collection("biz_log_tenant_subapp");

		private static String collection(String collection) {
			return PREFIX.concat("_").concat(collection);
		}
	}
}
