package io.github.lijiajia3515.cairo.auth.framework.security.tenant_subapp_user;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.EndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantEndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantSubappMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.AppNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.EndpointNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SubappNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantAppNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantSubappDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantSubappNotApplyException;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_department.TenantAppDepartmentCommonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 企业子应用用户准入闸口矩阵：应用/终端/子应用/企业四级状态检查 + 端/子应用两级 AccessScope 闸门
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CairoAuthTenantSubappUserServiceCheckSystemStatusTest {

	@Mock
	private MongoTemplate readMongoTemplate;
	@Mock
	private RedisTemplate<String, Object> redisTemplate;
	@Mock
	private TenantAppDepartmentCommonService tenantAppDepartmentCommonService;

	private CairoAuthTenantSubappUserService service;

	@BeforeEach
	void setUp() {
		service = new CairoAuthTenantSubappUserService(readMongoTemplate, redisTemplate, tenantAppDepartmentCommonService);
	}

	private void call() {
		service.checkSystemStatus("t1", "cairo", "web", "manage", "v1");
	}

	// ---------- 基础链：存在性与启用状态 ----------

	@Test
	void 应用不存在_拒绝() {
		when(readMongoTemplate.findOne(any(Query.class), eq(AppMongodb.class), eq(MongodbConstants.Collection.APP))).thenReturn(null);
		assertThatThrownBy(this::call).isInstanceOf(AppNotFoundException.class);
	}

	@Test
	void 应用禁用_拒绝() {
		stubApp(false);
		assertThatThrownBy(this::call).isInstanceOf(AppDisabledException.class);
	}

	@Test
	void 终端不存在_拒绝() {
		stubEnabledBasics("public", "public");
		when(readMongoTemplate.findOne(any(Query.class), eq(EndpointMongodb.class), eq(MongodbConstants.Collection.ENDPOINT))).thenReturn(null);
		assertThatThrownBy(this::call).isInstanceOf(EndpointNotFoundException.class);
	}

	@Test
	void 终端禁用_拒绝() {
		stubEnabledBasics("public", "public");
		when(readMongoTemplate.findOne(any(Query.class), eq(EndpointMongodb.class), eq(MongodbConstants.Collection.ENDPOINT))).thenReturn(endpoint(false, "public"));
		assertThatThrownBy(this::call).isInstanceOf(EndpointDisabledException.class);
	}

	@Test
	void 子应用不存在_拒绝() {
		stubEnabledBasics("public", "public");
		when(readMongoTemplate.findOne(any(Query.class), eq(SubappMongodb.class), eq(MongodbConstants.Collection.SUBAPP))).thenReturn(null);
		assertThatThrownBy(this::call).isInstanceOf(SubappNotFoundException.class);
	}

	@Test
	void 子应用禁用_拒绝() {
		stubEnabledBasics("public", "public");
		when(readMongoTemplate.findOne(any(Query.class), eq(SubappMongodb.class), eq(MongodbConstants.Collection.SUBAPP))).thenReturn(subapp(false, "public"));
		assertThatThrownBy(this::call).isInstanceOf(SubappDisabledException.class);
	}

	@Test
	void 企业不存在_拒绝() {
		stubEnabledBasics("public", "public");
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantMongodb.class), eq(MongodbConstants.Collection.TENANT))).thenReturn(null);
		assertThatThrownBy(this::call).isInstanceOf(TenantNotFoundException.class);
	}

	@Test
	void 企业禁用_拒绝() {
		stubEnabledBasics("public", "public");
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantMongodb.class), eq(MongodbConstants.Collection.TENANT))).thenReturn(tenant(false));
		assertThatThrownBy(this::call).isInstanceOf(TenantDisabledException.class);
	}

	@Test
	void 企业未开通应用_拒绝() {
		stubEnabledBasics("public", "public");
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantAppMongodb.class), eq(MongodbConstants.Collection.TENANT_APP))).thenReturn(null);
		assertThatThrownBy(this::call).isInstanceOf(TenantAppNotApplyException.class);
	}

	@Test
	void 企业应用禁用_拒绝() {
		stubEnabledBasics("public", "public");
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantAppMongodb.class), eq(MongodbConstants.Collection.TENANT_APP))).thenReturn(tenantApp(false));
		assertThatThrownBy(this::call).isInstanceOf(TenantAppDisabledException.class);
	}

	// ---------- 端闸门（endpoint.scope） ----------

	@Test
	void 平台级终端_企业硬拒() {
		stubEnabledBasics("app", "public");
		assertThatThrownBy(this::call).isInstanceOf(TenantEndpointNotApplyException.class);
		// 硬拒在开通记录查询之前，不产生 tenant_endpoint 查询
		verify(readMongoTemplate, never()).findOne(any(Query.class), eq(TenantEndpointMongodb.class), eq(MongodbConstants.Collection.TENANT_ENDPOINT));
	}

	@Test
	void 企业级终端_未开通_拒绝() {
		stubEnabledBasics("tenant", "public");
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantEndpointMongodb.class), eq(MongodbConstants.Collection.TENANT_ENDPOINT))).thenReturn(null);
		assertThatThrownBy(this::call).isInstanceOf(TenantEndpointNotApplyException.class);
	}

	@Test
	void 企业级终端_开通记录禁用_拒绝() {
		stubEnabledBasics("tenant", "public");
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantEndpointMongodb.class), eq(MongodbConstants.Collection.TENANT_ENDPOINT))).thenReturn(tenantEndpoint(false));
		assertThatThrownBy(this::call).isInstanceOf(TenantEndpointDisabledException.class);
	}

	@Test
	void 企业级终端_已开通_放行() {
		stubEnabledBasics("tenant", "public");
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantEndpointMongodb.class), eq(MongodbConstants.Collection.TENANT_ENDPOINT))).thenReturn(tenantEndpoint(true));
		assertThatCode(this::call).doesNotThrowAnyException();
	}

	@Test
	void 开放级终端_直通不查开通记录() {
		stubEnabledBasics("public", "public");
		assertThatCode(this::call).doesNotThrowAnyException();
		verify(readMongoTemplate, never()).findOne(any(Query.class), eq(TenantEndpointMongodb.class), eq(MongodbConstants.Collection.TENANT_ENDPOINT));
	}

	@Test
	void 终端范围缺省_按企业级处理需开通() {
		stubEnabledBasics(null, "public");
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantEndpointMongodb.class), eq(MongodbConstants.Collection.TENANT_ENDPOINT))).thenReturn(null);
		assertThatThrownBy(this::call).isInstanceOf(TenantEndpointNotApplyException.class);
	}

	// ---------- 子应用闸门（subapp.scope） ----------

	@Test
	void 平台级子应用_企业硬拒() {
		stubEnabledBasics("public", "app");
		assertThatThrownBy(this::call).isInstanceOf(TenantSubappNotApplyException.class);
		verify(readMongoTemplate, never()).findOne(any(Query.class), eq(TenantSubappMongodb.class), eq(MongodbConstants.Collection.TENANT_SUBAPP));
	}

	@Test
	void 企业级子应用_未开通_拒绝() {
		stubEnabledBasics("public", "tenant");
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantSubappMongodb.class), eq(MongodbConstants.Collection.TENANT_SUBAPP))).thenReturn(null);
		assertThatThrownBy(this::call).isInstanceOf(TenantSubappNotApplyException.class);
	}

	@Test
	void 企业级子应用_开通记录禁用_拒绝() {
		stubEnabledBasics("public", "tenant");
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantSubappMongodb.class), eq(MongodbConstants.Collection.TENANT_SUBAPP))).thenReturn(tenantSubapp(false));
		assertThatThrownBy(this::call).isInstanceOf(TenantSubappDisabledException.class);
	}

	@Test
	void 企业级子应用_已开通_放行() {
		stubEnabledBasics("public", "tenant");
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantSubappMongodb.class), eq(MongodbConstants.Collection.TENANT_SUBAPP))).thenReturn(tenantSubapp(true));
		assertThatCode(this::call).doesNotThrowAnyException();
	}

	@Test
	void 子应用范围缺省_随终端放行不查开通记录() {
		stubEnabledBasics("public", null);
		assertThatCode(this::call).doesNotThrowAnyException();
		verify(readMongoTemplate, never()).findOne(any(Query.class), eq(TenantSubappMongodb.class), eq(MongodbConstants.Collection.TENANT_SUBAPP));
	}

	// ---------- 造数 ----------

	/** 全链基础存根：应用/终端/子应用/企业/企业应用 均 enabled，scope 由参数指定 */
	private void stubEnabledBasics(String endpointScope, String subappScope) {
		stubApp(true);
		when(readMongoTemplate.findOne(any(Query.class), eq(EndpointMongodb.class), eq(MongodbConstants.Collection.ENDPOINT))).thenReturn(endpoint(true, endpointScope));
		when(readMongoTemplate.findOne(any(Query.class), eq(SubappMongodb.class), eq(MongodbConstants.Collection.SUBAPP))).thenReturn(subapp(true, subappScope));
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantMongodb.class), eq(MongodbConstants.Collection.TENANT))).thenReturn(tenant(true));
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantAppMongodb.class), eq(MongodbConstants.Collection.TENANT_APP))).thenReturn(tenantApp(true));
	}

	private void stubApp(boolean enabled) {
		when(readMongoTemplate.findOne(any(Query.class), eq(AppMongodb.class), eq(MongodbConstants.Collection.APP))).thenReturn(AppMongodb.builder().appId("cairo").enabled(enabled).build());
	}

	private EndpointMongodb endpoint(boolean enabled, String scope) {
		return EndpointMongodb.builder().appId("cairo").endpointId("web").enabled(enabled).scope(scope).build();
	}

	private SubappMongodb subapp(boolean enabled, String scope) {
		return SubappMongodb.builder().appId("cairo").endpointId("web").subappId("manage").enabled(enabled).scope(scope).build();
	}

	private TenantMongodb tenant(boolean enabled) {
		return TenantMongodb.builder().tenantId("t1").enabled(enabled).build();
	}

	private TenantAppMongodb tenantApp(boolean enabled) {
		return TenantAppMongodb.builder().tenantId("t1").appId("cairo").enabled(enabled).build();
	}

	private TenantEndpointMongodb tenantEndpoint(boolean enabled) {
		return TenantEndpointMongodb.builder().tenantId("t1").appId("cairo").endpointId("web").enabled(enabled).build();
	}

	private TenantSubappMongodb tenantSubapp(boolean enabled) {
		return TenantSubappMongodb.builder().tenantId("t1").appId("cairo").endpointId("web").subappId("manage").enabled(enabled).build();
	}
}
