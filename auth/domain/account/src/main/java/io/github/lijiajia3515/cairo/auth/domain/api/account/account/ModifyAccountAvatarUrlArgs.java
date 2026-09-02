package io.github.lijiajia3515.cairo.auth.domain.api.account.account;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改账号头像参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModifyAccountAvatarUrlArgs {
	/**
	 * 头像
	 */
	@NotNull
	private String avatarUrl;
}
