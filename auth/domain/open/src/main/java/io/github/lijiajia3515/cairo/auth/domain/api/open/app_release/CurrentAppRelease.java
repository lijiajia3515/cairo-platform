package io.github.lijiajia3515.cairo.auth.domain.api.open.app_release;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentAppRelease implements Serializable {

	/**
	 * 应用版本
	 */
	private String appVersion;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 描述
	 */
	private String remark;

	/**
	 * 更新时间
	 */
	private LocalDateTime updateTime;
}
