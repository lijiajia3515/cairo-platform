package io.github.lijiajia3515.cairo.auth.framework.security.tenant_app_user;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.ClientMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.EndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantEndpointMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.ClientNotFoundException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointDisabledException;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.TenantEndpointNotApplyException;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoAuthCommonService;
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
 * 企业应用用户准入闸口：客户端检查 + 端闸门（无子应用级闸门，子应用检查独立在 checkSubappStatus）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CairoAuthTenantAppUserServiceCheckSystemStatusTest {

	@Mock
	private MongoTemplate readMongoTemplate;
	@Mock
	private RedisTemplate<String, Object> redisTemplate;
	@Mock
	private CairoAuthCommonService cairoAuthCommonService;
	@Mock
	private TenantAppDepartmentCommonService tenantAppDepartmentCommonService;

	private CairoAuthTenantAppUserService service;

	@BeforeEach
	void setUp() {
		service = new CairoAuthTenantAppUserService(readMongoTemplate, redisTemplate, cairoAuthCommonService, tenantAppDepartmentCommonService);
	}

	private void call() {
		service.checkSystemStatus("t1", "cairo", "web", "client1");
	}

	@Test
	void 客户端不存在_拒绝() {
		stubEnabledBasics("public");
		when(readMongoTemplate.findOne(any(Query.class), eq(ClientMongodb.class), eq(MongodbConstants.Collection.CLIENT))).thenReturn(null);
		assertThatThrownBy(this::call).isInstanceOf(ClientNotFoundException.class);
	}

	@Test
	void 客户端禁用_拒绝() {
		stubEnabledBasics("public");
		when(readMongoTemplate.findOne(any(Query.class), eq(ClientMongodb.class), eq(MongodbConstants.Collection.CLIENT))).thenReturn(client(false));
		assertThatThrownBy(this::call).isInstanceOf(ClientDisabledException.class);
	}

	@Test
	void 平台级终端_企业硬拒() {
		stubEnabledBasics("app");
		assertThatThrownBy(this::call).isInstanceOf(TenantEndpointNotApplyException.class);
		verify(readMongoTemplate, never()).findOne(any(Query.class), eq(TenantEndpointMongodb.class), eq(MongodbConstants.Collection.TENANT_ENDPOINT));
	}

	@Test
	void 企业级终端_未开通_拒绝() {
		stubEnabledBasics("tenant");
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantEndpointMongodb.class), eq(MongodbConstants.Collection.TENANT_ENDPOINT))).thenReturn(null);
		assertThatThrownBy(this::call).isInstanceOf(TenantEndpointNotApplyException.class);
	}

	@Test
	void 企业级终端_开通记录禁用_拒绝() {
		stubEnabledBasics("tenant");
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantEndpointMongodb.class), eq(MongodbConstants.Collection.TENANT_ENDPOINT))).thenReturn(tenantEndpoint(false));
		assertThatThrownBy(this::call).isInstanceOf(TenantEndpointDisabledException.class);
	}

	@Test
	void 企业级终端_已开通_放行() {
		stubEnabledBasics("tenant");
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantEndpointMongodb.class), eq(MongodbConstants.Collection.TENANT_ENDPOINT))).thenReturn(tenantEndpoint(true));
		assertThatCode(this::call).doesNotThrowAnyException();
	}

	@Test
	void 开放级终端_直通不查开通记录() {
		stubEnabledBasics("public");
		assertThatCode(this::call).doesNotThrowAnyException();
		verify(readMongoTemplate, never()).findOne(any(Query.class), eq(TenantEndpointMongodb.class), eq(MongodbConstants.Collection.TENANT_ENDPOINT));
	}

	private void stubEnabledBasics(String endpointScope) {
		when(readMongoTemplate.findOne(any(Query.class), eq(AppMongodb.class), eq(MongodbConstants.Collection.APP))).thenReturn(AppMongodb.builder().appId("cairo").enabled(true).build());
		when(readMongoTemplate.findOne(any(Query.class), eq(ClientMongodb.class), eq(MongodbConstants.Collection.CLIENT))).thenReturn(client(true));
		when(readMongoTemplate.findOne(any(Query.class), eq(EndpointMongodb.class), eq(MongodbConstants.Collection.ENDPOINT))).thenReturn(
			EndpointMongodb.builder().appId("cairo").endpointId("web").enabled(true).scope(endpointScope).build());
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantMongodb.class), eq(MongodbConstants.Collection.TENANT))).thenReturn(TenantMongodb.builder().tenantId("t1").enabled(true).build());
		when(readMongoTemplate.findOne(any(Query.class), eq(TenantAppMongodb.class), eq(MongodbConstants.Collection.TENANT_APP))).thenReturn(TenantAppMongodb.builder().tenantId("t1").appId("cairo").enabled(true).build());
	}

	private ClientMongodb client(boolean enabled) {
		return ClientMongodb.builder().clientId("client1").enabled(enabled).build();
	}

	private TenantEndpointMongodb tenantEndpoint(boolean enabled) {
		return TenantEndpointMongodb.builder().tenantId("t1").appId("cairo").endpointId("web").enabled(enabled).build();
	}
}
