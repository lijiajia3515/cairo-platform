package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * 元信息
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TenantAppUserMetadataMongodb {

	/**
	 * 创建用户
	 * 创建该记录的用户ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String createUserId;

	/**
	 * 创建时间
	 * 记录创建时间
	 */
	@CreatedDate
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime createTime;

	/**
	 * 更新用户
	 * 最后更新该记录的用户ID
	 */
	@Field(write = Field.Write.ALWAYS)
	private String updateUserId;

	/**
	 * 最后更新时间
	 * 记录最后更新时间
	 */
	@LastModifiedDate
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime updateTime;
}
