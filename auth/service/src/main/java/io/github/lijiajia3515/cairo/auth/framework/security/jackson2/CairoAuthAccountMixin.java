package io.github.lijiajia3515.cairo.auth.framework.security.jackson2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.lijiajia3515.cairo.auth.framework.security.core.LoginType;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CairoAuthAccountMixin {

    @JsonCreator
    CairoAuthAccountMixin(
            @JsonProperty("id") String id,
            @JsonProperty("loginType") LoginType loginType,
			@JsonProperty("snsType") String snsType,
            @JsonProperty("accountId") String accountId,
            @JsonProperty("nickname") String nickname,
            @JsonProperty("loginname") String loginname,
            @JsonProperty("password") String password,
            @JsonProperty("phoneNumber") String phoneNumber,
            @JsonProperty("email") String email,
            @JsonProperty("avatarUrl") String avatarUrl,
			@JsonProperty("enabled") boolean enabled,
			@JsonProperty("locked") boolean locked,
            @JsonProperty("authorities") Collection<GrantedAuthority> authorities,
            @JsonProperty("attributes") Map<String, Object> attributes) {

    }
}
