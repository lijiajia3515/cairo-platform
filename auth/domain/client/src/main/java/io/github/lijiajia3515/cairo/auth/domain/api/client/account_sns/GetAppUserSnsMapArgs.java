package io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 获取联合OpenId
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAppUserSnsMapArgs implements Serializable {

	/**
	 * 第三方认证类型
	 */
	@NotNull
	private String snsType;

	/**
	 * 第三方认证提供商ID
	 */
	@NotNull
	private String snsProviderId;

	/**
	 * 应用用户ID数组
	 */
	@NotNull
	@Size(min = 1)
	private List<String> appUserIds;
}
