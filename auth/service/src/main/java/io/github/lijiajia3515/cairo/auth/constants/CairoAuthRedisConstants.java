package io.github.lijiajia3515.cairo.auth.constants;


public class CairoAuthRedisConstants {

    public interface Keys {

		String ACCOUNT_ACCESS_TOKEN = "account_access_token";


		String APP_USER_ACCESS_TOKEN = "app_user_access_token";


		String TENANT_APP_USER_ACCESS_TOKEN = "tenant_app_user_access_token";

		/**
		 * 认证账号缓存key
		 */
		String AUTH_CLIENT = "auth:client";

		/**
		 * 认证账号缓存key
		 */
		String AUTH_CLIENT_ID = "auth:client_id";

		/**
		 * 认证账号缓存key
		 */
		String AUTH_ACCOUNT = "auth:account";


        /**
         * 认证企业应用级用户缓存key
         */
        String AUTH_TENANT_APP_USER = "auth:tenant_app_user";

		/**
		 * 认证应用级用户缓存key
		 */
		String AUTH_APP_USER = "auth:app_user";


        /**
         * 企业缓存key
         */
        String TENANT = "tenant";

		String SMS_TEMPLATE = "sms_template";

		String NOTIFY_TEMPLATE = "notify_template";
    }
}
