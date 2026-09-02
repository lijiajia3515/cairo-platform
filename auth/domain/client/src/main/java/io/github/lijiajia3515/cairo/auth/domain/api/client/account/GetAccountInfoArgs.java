package io.github.lijiajia3515.cairo.auth.domain.api.client.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取账号列表参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAccountInfoArgs implements Serializable {

	/**
	 * 账号ID
	 */
	private String accountId;


	@Builder.Default
	private Map<String, String> extension = new HashMap<>();

}
