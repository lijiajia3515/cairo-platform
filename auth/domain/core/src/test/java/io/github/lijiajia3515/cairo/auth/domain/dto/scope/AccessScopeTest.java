package io.github.lijiajia3515.cairo.auth.domain.dto.scope;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 准入范围枚举测试：三级语义与解析行为
 */
class AccessScopeTest {

	@Test
	void 应有且仅有三级() {
		assertThat(AccessScope.values()).hasSize(3);
	}

	@Test
	void 三级取值() {
		assertThat(AccessScope.PUBLIC.getScopeValue()).isEqualTo("public");
		assertThat(AccessScope.APP.getScopeValue()).isEqualTo("app");
		assertThat(AccessScope.TENANT.getScopeValue()).isEqualTo("tenant");
	}

	@Test
	void 按值解析() {
		assertThat(AccessScope.scopeValueOf("public")).contains(AccessScope.PUBLIC);
		assertThat(AccessScope.scopeValueOf("app")).contains(AccessScope.APP);
		assertThat(AccessScope.scopeValueOf("tenant")).contains(AccessScope.TENANT);
	}

	@Test
	void 未知值与空值解析为空() {
		assertThat(AccessScope.scopeValueOf("project")).isEqualTo(Optional.empty());
		assertThat(AccessScope.scopeValueOf("")).isEqualTo(Optional.empty());
		assertThat(AccessScope.scopeValueOf(null)).isEqualTo(Optional.empty());
	}
}
