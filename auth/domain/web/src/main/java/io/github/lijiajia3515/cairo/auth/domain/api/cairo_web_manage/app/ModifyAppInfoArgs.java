package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 修改应用 参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyAppInfoArgs implements Serializable {

	/**
	 * 应用ID
	 */
	@NotNull
	@NotBlank
	private String appId;


	/**
	 * 应用名称
	 */
	private String appName;

	/**
	 * 图标值
	 */
	private String icon;

	/**
	 * 范围
	 */
	private List<String> scopes;

	/**
	 * 是否内部应用
	 */
	private Boolean privateApp;


	/**
	 * 是否开启自动注册
	 */
	private Boolean autoRegister;

	/**
	 * 管理员账号
	 */
	private List<String> adminAccountIds;


}
