package io.github.lijiajia3515.cairo.auth.domain.api.open.sns;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 获取联接OpenId参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetSnsInfoArgs implements Serializable {

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
	 * 第三方认证授权码
	 */
	@NotNull
	private String snsCode;
}
