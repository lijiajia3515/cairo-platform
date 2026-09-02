package io.github.lijiajia3515.cairo.auth.modules.file;

import io.github.lijiajia3515.cairo.core.business.Business;
import lombok.Getter;
import lombok.experimental.Accessors;


@Getter
@Accessors(fluent = true)
public enum FileBusiness implements Business {
	/**
	 * 业务默认成功结果
	 */
	UPLOAD_FAILED("File.UploadFailed", "文件上传失败"),
	SIGN_FAILED("File.SignFailed", "文件签名失败"),

	;

	public final String code;
	public final String message;

	FileBusiness(String code, String message) {
		this.code = code;
		this.message = message;
	}
}
