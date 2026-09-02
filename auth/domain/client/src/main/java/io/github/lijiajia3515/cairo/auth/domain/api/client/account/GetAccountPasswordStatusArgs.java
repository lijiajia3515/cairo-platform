package io.github.lijiajia3515.cairo.auth.domain.api.client.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
/**
 * 获取账号密码状态
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAccountPasswordStatusArgs implements Serializable {

	/**
	 * 账号ID
	 */
	@NotNull
	@NotBlank
	private String accountId;


}
