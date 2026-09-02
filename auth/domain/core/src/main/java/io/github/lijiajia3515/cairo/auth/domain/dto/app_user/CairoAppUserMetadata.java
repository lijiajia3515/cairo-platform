package io.github.lijiajia3515.cairo.auth.domain.dto.app_user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 源数据 对象
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CairoAppUserMetadata implements Serializable {

	/**
	 * 创建用户
	 */
	private AppUser createUser;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 修改用户
	 */
	private AppUser updateUser;

	/**
	 * 最后修改时间
	 */
	private LocalDateTime updateTime;
}
