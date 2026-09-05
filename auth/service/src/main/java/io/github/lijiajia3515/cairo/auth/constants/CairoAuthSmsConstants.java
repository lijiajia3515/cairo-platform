package io.github.lijiajia3515.cairo.auth.constants;

/**
 * 认证服务短信业务配置
 */
public interface CairoAuthSmsConstants {

	/**
	 * 验证码
	 */
	interface VerifyCode {
		/**
		 * 短信编码
		 */
		String BIZ_ID = "VerifyCode";

		/**
		 * 验证码参数名称
		 */
		String PARAM_CODE = "code";

	}

	// ========== 账号级别模板 start ==========

	/**
	 * 注册账号成功
	 */
	interface RegisterAccountSuccess {
		/**
		 * 短信编码
		 */
		String BIZ_ID = "RegisterAccountSuccess";

		/**
		 * 昵称
		 */
		String PARAM_NAME = "name";
		/**
		 * 登录名
		 */
		String PARAM_USERNAME = "username";

		/**
		 * 密码
		 */
		String PARAM_PASSWORD = "password";
	}

	/**
	 * 注销账号通知
	 */
	interface LogoffAccount {
		/**
		 * 短信编码
		 */
		String BIZ_ID = "LogoffAccount";

		/**
		 * 账号
		 */
		String PARAM_ACCOUNT = "Account";

		/**
		 * 密码
		 */
		String PARAM_DAY = "Day";
	}

	/**
	 * 注销账号成功通知
	 */
	interface LogoffAccountSuccess {
		/**
		 * 短信编码
		 */
		String BIZ_ID = "LogoffAccountSuccess";

		/**
		 * 账号
		 */
		String PARAM_ACCOUNT = "Account";
	}

	/**
	 * 取消注销账号成功通知
	 */
	interface UnlogoffAccount {
		/**
		 * 短信编码
		 */
		String BIZ_ID = "UnlogoffAccount";

		/**
		 * 账号
		 */
		String PARAM_ACCOUNT = "Account";
	}

	// ========== 账号级别模板 end ==========

	// ========== 应用级别模板 start ==========

	/**
	 * 注册应用级用户成功通知
	 */
	interface RegisterAppUserSuccess {

		/**
		 * 账号
		 */
		String BIZ_ID = "RegisterAppUserSuccess";

		/**
		 * 账号
		 */
		String PARAM_ACCOUNT = "Account";
		/**
		 * 用户
		 */
		String PARAM_USER = "User";
	}

	/**
	 * 注销应用级用户通知
	 */
	interface LogoffAppUser {
		/**
		 * 业务ID
		 */
		String BIZ_ID = "LogoffAppUser";

		/**
		 * 账号
		 */
		String PARAM_ACCOUNT = "Account";

		/**
		 * 用户
		 */
		String PARAM_USER = "User";
		/**
		 * 注销天数
		 */
		String PARAM_DAY = "Day";
	}

	/**
	 * 注销企业应用级用户成功通知
	 */
	interface LogoffAppUserSuccess {
		/**
		 * 短信编码
		 */
		String BIZ_ID = "LogoffAppUserSuccess";
		/**
		 * 账号
		 */
		String PARAM_ACCOUNT = "Account";

		/**
		 * 用户
		 */
		String PARAM_USER = "User";
	}

	/**
	 * 取消注销应用级用户成功通知
	 */
	interface UnlogoffAppUser {
		/**
		 * 短信编码
		 */
		String BIZ_ID = "UnlogoffAppUser";
		/**
		 * 账号
		 */
		String PARAM_ACCOUNT = "Account";

		/**
		 * 用户
		 */
		String PARAM_USER = "User";
	}
	// ========== 应用级别模板 end ==========

	// ========== 企业级别模板 start ==========
	/**
	 * 注册企业成功
	 */
	interface RegisterTenantSuccess {

		/**
		 * 短信编码
		 */
		String BIZ_ID = "RegisterTenantSuccess";

		/**
		 * 账号
		 */
		String PARAM_ACCOUNT = "Account";
		/**
		 * 企业
		 */
		String PARAM_TENANT = "Tenant";
	}
	// ========== 企业级别模板 end ==========

	// ========== 企业应用级别模板 start ==========

	/**
	 * 申请企业应用成功
	 */
	interface ApplyNewTenantAppSuccess {
		/**
		 * 业务ID
		 */
		String BIZ_ID = "ApplyNewTenantAppSuccess";

		/**
		 * 账号
		 */
		String PARAM_ACCOUNT = "Account";
		/**
		 * 企业
		 */
		String PARAM_TENANT = "Tenant";
	}



	/**
	 * 注册企业应用级用户成功通知
	 */
	interface RegisterTenantAppUserSuccess {

		/**
		 * 账号ID
		 */
		String BIZ_ID = "RegisterTenantAppUserSuccess";

		/**
		 * 账号
		 */
		String PARAM_ACCOUNT = "Account";
		/**
		 * 企业
		 */
		String PARAM_TENANT = "Tenant";
		/**
		 * 用户
		 */
		String PARAM_USER = "User";
	}

	/**
	 * 注销企业应用级用户通知
	 */
	interface LogoffTenantAppUser {


		/**
		 * 业务ID
		 */
		String BIZ_ID = "LogoffTenantAppUser";

		/**
		 * 账号
		 */
		String PARAM_ACCOUNT = "Account";

		/**
		 * 企业
		 */
		String PARAM_TENANT = "Tenant";

		/**
		 * 用户
		 */
		String PARAM_USER = "User";
		/**
		 * 注销天数
		 */
		String PARAM_DAY = "Day";
	}

	/**
	 * 注销企业应用级用户成功通知
	 */
	interface LogoffTenantAppUserSuccess {

		/**
		 * 业务ID
		 */
		String BIZ_ID = "LogoffTenantAppUserSuccess";

		/**
		 * 账号
		 */
		String PARAM_ACCOUNT = "Account";
		/**
		 * 企业
		 */
		String PARAM_TENANT = "Tenant";
		/**
		 * 用户
		 */
		String PARAM_USER = "User";
	}

	/**
	 * 取消注销企业应用级用户成功通知
	 */
	interface UnlogoffTenantAppUser {
		/**
		 * 短信编码
		 */
		String BIZ_ID = "UnlogoffTenantAppUser";

		/**
		 * 账号
		 */
		String PARAM_ACCOUNT = "Account";

		/**
		 * 企业
		 */
		String PARAM_TENANT = "Tenant";

		/**
		 * 用户
		 */
		String PARAM_USER = "User";
	}

	// ========== 企业应用级别模板 end ==========
}
