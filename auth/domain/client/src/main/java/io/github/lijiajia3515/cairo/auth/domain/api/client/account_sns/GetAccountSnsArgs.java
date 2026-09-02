package io.github.lijiajia3515.cairo.auth.domain.api.client.account_sns;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 获取账号三方绑定参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAccountSnsArgs {
	/**
	 * 账号id
	 */
	@NotEmpty
	private String accountId;

	/**
	 * 三方类型
	 */
	@NotEmpty
	private List<String> snsTypes;

}
