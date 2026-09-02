package io.github.lijiajia3515.cairo.auth.domain.api.subapp.account;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 根据id获取账号参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAccountInfoArgs {
	/**
	 * 账号ID
	 */
	@NotNull
	private String accountId;



	@Builder.Default
	private Map<String, String> extension = new HashMap<>();
}
