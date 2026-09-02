package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.account;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 删除账号
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteAccountArgs implements Serializable {

	/**
	 * 账号id
	 */
	@NotNull
	private String accountId;
}
