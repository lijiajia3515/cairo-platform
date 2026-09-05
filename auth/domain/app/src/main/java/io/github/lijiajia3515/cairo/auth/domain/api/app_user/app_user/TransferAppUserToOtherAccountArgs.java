package io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 转移应用级用户至其他账号
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TransferAppUserToOtherAccountArgs implements Serializable {

	/**
	 * 应用级用户ID
	 */
	@NotNull
	private String userId;

	/**
	 * 其他账号ID
	 */
	@NotNull
	private String otherAccountId;
}
