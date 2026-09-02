package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.subapp_version;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 删除子应用版本
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class SyncSubappVersionArgs implements Serializable {
	/**
	 * 数据来源子应用ID
	 */
	@NotNull
	private String sourceSubappId;

	/**
	 * 数据来源子应用版本
	 */
	@NotNull
	private String sourceSubappVersion;

	/**
	 * 数据变动子应用ID
	 */
	@NotNull
	private String changeSubappId;

	/**
	 * 数据变动子应用版本号
	 */
	@NotNull
	private String changeSubappVersion;

}
