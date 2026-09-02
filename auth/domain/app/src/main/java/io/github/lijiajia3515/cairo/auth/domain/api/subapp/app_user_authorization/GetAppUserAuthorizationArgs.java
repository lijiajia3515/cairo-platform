package io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_authorization;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class GetAppUserAuthorizationArgs extends AbstractPage<GetAppUserAuthorizationArgs> implements Serializable {
	/**
	 * 终端ID
	 */
	private String endpointId;

	/**
	 * 客户端ID
	 */
	private String clientId;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 关键字搜索
	 */
	private String keyword;

	/**
	 * 状态
	 */
	private String status;

}
