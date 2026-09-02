package io.github.lijiajia3515.cairo.auth.domain.api.tenant_endpoint.tenant_app_user_authorization;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 企业用户会话查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetMyTenantAppUserAuthorizationArgs extends AbstractPage<GetMyTenantAppUserAuthorizationArgs> implements Serializable {

}
