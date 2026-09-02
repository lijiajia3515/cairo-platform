package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 应用创建请求
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateAppArgs implements Serializable {

	/**
	 * 应用id
	 */
	@NotNull
	@NotBlank
	private String appId;

	/**
	 * 应用名称
	 */
	@NotNull
	@NotBlank
	private String appName;

	/**
	 * 范围
	 */
	@NotNull
	@NotEmpty
	private List<String> scopes;

	/**
	 * 是否内部应用
	 */
	@Builder.Default
	private Boolean privateApp = false;

	/**
	 * 管理员账号ID
	 */
	private List<String> adminAccountIds;

	/**
	 * 自动注册
	 */
	private boolean autoRegister;

	/**
	 * 图标值
	 */
	private String icon;

	/**
	 * 启用状态
	 */
	@Builder.Default
	private boolean enabled = false;
}
