package io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user_authorization;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 应用级用户会话查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetMyAppUserAuthorizationArgs extends AbstractPage<GetMyAppUserAuthorizationArgs> implements Serializable {
	/**
	 * 关键字查询
	 */
	private String keyword;
}
