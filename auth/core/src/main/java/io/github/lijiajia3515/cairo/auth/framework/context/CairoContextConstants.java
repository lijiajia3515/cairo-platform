package io.github.lijiajia3515.cairo.auth.framework.context;

public class CairoContextConstants {
    public static final String CAIRO_TAG = "cairoTag";
    public static final CairoContextModel CAIRO_TAG_CONTEXT = CairoContextModel.builder().headerName("x-cairo-tag").parameterName("x-cairo-tag").defaultValue("").build();

    public static final String CLIENT_ID = "clientId";
    public static final CairoContextModel CLIENT_ID_CONTEXT = CairoContextModel.builder().headerName("client-id").parameterName("client_id").build();

    public static final String ENDPOINT_ID = "endpointId";
    public static final CairoContextModel ENDPOINT_ID_CONTEXT = CairoContextModel.builder().headerName("endpoint-id").parameterName("endpoint_id").build();

    public static final String APP_ID = "appId";
    public static final CairoContextModel APP_ID_CONTEXT = CairoContextModel.builder().headerName("app-id").parameterName("app_id").build();

    public static final String TENANT_ID = "tenantId";
    public static final CairoContextModel TENANT_ID_CONTEXT = CairoContextModel.builder().headerName("tenant-id").parameterName("tenant_id").build();

	public static final String SUBAPP_ID = "subappId";
	public static final CairoContextModel SUBAPP_ID_CONTEXT = CairoContextModel.builder().headerName("subapp-id").parameterName("subapp_id").build();

	public static final String SUBAPP_VERSION = "subappVersion";
	public static final CairoContextModel SUBAPP_VERSION_CONTEXT = CairoContextModel.builder().headerName("subapp-version").parameterName("subapp_version").build();


}
