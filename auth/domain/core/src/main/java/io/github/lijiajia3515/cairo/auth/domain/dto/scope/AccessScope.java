package io.github.lijiajia3515.cairo.auth.domain.dto.scope;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 准入范围（终端/子应用共用的准入策略，按访问主体分三级）
 * <p>
 * 三级语义：public 任何人可用；app 仅平台运营人员可用（如各应用的管理后台）；
 * tenant 需企业开通对应记录后可用。层级互斥，app 级对企业侧不可见。
 */
@Getter
public enum AccessScope {
	/**
	 * 开放：无需企业身份即可使用
	 */
	PUBLIC("public"),

	/**
	 * 平台：平台运营人员专用，企业侧不可用（如应用管理后台）
	 */
	APP("app"),

	/**
	 * 企业：需企业开通对应记录
	 */
	TENANT("tenant"),
	;
	/**
	 * 范围值
	 */
	public final String scopeValue;

	AccessScope(String scopeValue) {
		this.scopeValue = scopeValue;
	}

	/**
	 * 查找 准入范围
	 *
	 * @param scopeValue 范围值
	 * @return 准入范围
	 */
	public static Optional<AccessScope> scopeValueOf(String scopeValue) {
		return Arrays.stream(AccessScope.values()).filter(x -> x.scopeValue.equals(scopeValue)).findFirst();
	}
}
