package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.settings;

public class CairoSettingNames {
    private static final String SETTINGS_NAMESPACE = "settings.";

    private CairoSettingNames() {
    }

    /**
     * The names for client configuration settings.
     */
    public static final class Client {
        private static final String CLIENT_SETTINGS_NAMESPACE = SETTINGS_NAMESPACE.concat("client.");

        private Client() {
        }

    }

    /**
     * The names for authorization server configuration settings.
     */
    public static final class AuthorizationServer {
        private static final String AUTHORIZATION_SERVER_SETTINGS_NAMESPACE = SETTINGS_NAMESPACE.concat("authorization-server.");



        private AuthorizationServer() {
        }

    }

    /**
     * The names for token configuration settings.
     */
    public static final class Token {
        private static final String TOKEN_SETTINGS_NAMESPACE = SETTINGS_NAMESPACE.concat("token.");

		// id
        /**
         * accountAccessTokenFormat
         */
        public static final String ID_TOKEN_FORMAT = TOKEN_SETTINGS_NAMESPACE.concat("id-token-format");

		// account
		/**
		 *
		 * accountAccessTokenTimeToLive
		 */
		public static final String ID_TOKEN_TIME_TO_LIVE = TOKEN_SETTINGS_NAMESPACE.concat("id-token-time-to-live");

        /**
         * accountAccessTokenFormat
         */
        public static final String ACCOUNT_ACCESS_TOKEN_FORMAT = TOKEN_SETTINGS_NAMESPACE.concat("account-access-token-format");
        /**
         *
         * accountAccessTokenTimeToLive
         */
        public static final String ACCOUNT_ACCESS_TOKEN_TIME_TO_LIVE = TOKEN_SETTINGS_NAMESPACE.concat("account-access-token-time-to-live");
        /**
         *
         * accountRefreshTokenTimeToLive
         */
        public static final String ACCOUNT_REFRESH_TOKEN_TIME_TO_LIVE = TOKEN_SETTINGS_NAMESPACE.concat("account-refresh-token-time-to-live");
        /**
         * reuseAccountRefreshTokens
         */
        public static final String REUSE_ACCOUNT_REFRESH_TOKENS = TOKEN_SETTINGS_NAMESPACE.concat("reuse-account-refresh-tokens");

		// app endpoint user
		/**
		 * appUserAccessTokenFormat
		 */
		public static final String APP_USER_ACCESS_TOKEN_FORMAT = TOKEN_SETTINGS_NAMESPACE.concat("app-user-access-token-format");
		/**
		 *
		 * appUserAccessTokenTimeToLive
		 */
		public static final String APP_USER_ACCESS_TOKEN_TIME_TO_LIVE = TOKEN_SETTINGS_NAMESPACE.concat("app-user-access-token-time-to-live");
		/**
		 *
		 * appUserRefreshTokenTimeToLive
		 */
		public static final String APP_USER_REFRESH_TOKEN_TIME_TO_LIVE = TOKEN_SETTINGS_NAMESPACE.concat("app-user-refresh-token-time-to-live");

		/**
		 * reuseAppUserRefreshTokens
		 */
		public static final String REUSE_APP_USER_REFRESH_TOKENS = TOKEN_SETTINGS_NAMESPACE.concat("reuse-app-user-refresh-tokens");

		// tenant app endpoint user

        /**
         * TenantAppUserAccessTokenFormat
         */
        public static final String TENANT_APP_USER_ACCESS_TOKEN_FORMAT = TOKEN_SETTINGS_NAMESPACE.concat("tenant-app-user-access-token-format");

        /**
         *
         * TenantAppUserAccessTokenTimeToLive
         */
        public static final String TENANT_APP_USER_ACCESS_TOKEN_TIME_TO_LIVE = TOKEN_SETTINGS_NAMESPACE.concat("tenant-app-user-access-token-time-to-live");

        /**
         *
         * TenantAppUserRefreshTokenTimeToLive
         */
        public static final String TENANT_APP_USER_REFRESH_TOKEN_TIME_TO_LIVE = TOKEN_SETTINGS_NAMESPACE.concat("tenant-app-user-refresh-token-time-to-live");


        /**
         * reuseTenantAppUserRefreshTokens
         */
        public static final String REUSE_TENANT_APP_USER_REFRESH_TOKENS = TOKEN_SETTINGS_NAMESPACE.concat("reuse-tenant-app-user-refresh-tokens");

        private Token() {
        }

    }
}
