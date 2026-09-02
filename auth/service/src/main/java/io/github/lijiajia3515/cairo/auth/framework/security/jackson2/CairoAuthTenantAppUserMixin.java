package io.github.lijiajia3515.cairo.auth.framework.security.jackson2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoDepartment;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoRole;
import io.github.lijiajia3515.cairo.auth.framework.security.core.CairoTag;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true,value = {"credentialsNonExpired","accountNonLocked","AccountNonExpired","loginName"})
public class CairoAuthTenantAppUserMixin {

	@JsonCreator
	CairoAuthTenantAppUserMixin(
		@JsonProperty("id") String id,
		@JsonProperty("loginType") LoginType loginType,
		@JsonProperty("snsType") String snsType,
		@JsonProperty("appId") String appId,
		@JsonProperty("endpointId") String endpointId,
		@JsonProperty("clientId") String clientId,

		@JsonProperty("tenantId") String tenantId,
		@JsonProperty("userId") String userId,
		@JsonProperty("nickname") String nickname,
		@JsonProperty("phoneNumber") String phoneNumber,
		@JsonProperty("userEnabled") boolean userEnabled,
		@JsonProperty("appAdmin") boolean appAdmin,
		@JsonProperty("roles") List<CairoRole> roles,
		@JsonProperty("departments") List<CairoDepartment> departments,
		@JsonProperty("tags") List<CairoTag> tags,

		@JsonProperty("accountId") String accountId,
		@JsonProperty("accountNickname") String accountNickname,
		@JsonProperty("accountUsername") String accountUsername,
		@JsonProperty("accountPassword") String accountPassword,
		@JsonProperty("accountPhoneNumber") String accountPhoneNumber,
		@JsonProperty("accountEmail") String accountEmail,
		@JsonProperty("accountAvatarUrl") String accountAvatarUrl,
		@JsonProperty("accountEnabled") boolean accountEnabled,
		@JsonProperty("accountLocked") boolean accountLocked,

		@JsonProperty("authorities") Collection<GrantedAuthority> authorities,
		@JsonProperty("attributes") Map<String, Object> attributes) {
	}
}
