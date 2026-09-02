package io.github.lijiajia3515.cairo.auth.domain.dto.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 源数据 对象
 *
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CairoAccountMetadata implements Serializable {
	/**
	 * 创建账号
	 */
	private Account createAccount;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 最后修改人
	 */
	private Account updateAccount;
	/**
	 * 最后修改时间
	 */
	private LocalDateTime updateTime;
}
