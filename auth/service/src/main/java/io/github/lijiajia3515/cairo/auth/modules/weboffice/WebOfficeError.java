package io.github.lijiajia3515.cairo.auth.modules.weboffice;

import lombok.Getter;

public enum WebOfficeError {
	SUCCESS(0,"success"),
	E40002(40002,"用户凭证，即 x-weboffice-token 头, 无效"),
	E40003(400003,"用户操作权限不足"),
	E40004(40004,"文档不存在"),
	E40005(40005,"请求参数错误"),
	E40006(40006,"存储空间已满"),
	E40007(40007,"自定义错误，可以用来返回自定义错误信息"),
	E40008(40008,"文档名称冲突，例如重命名文档时"),
	E40009(40009,"文档版本不存在"),
	E40010(40010,"用户不存在"),
	E41001(41001,"文件未正确上传，例如保存文档时"),
	E50001(50001,"系统错误导致的请求不能正常响应"),


	SIGN_BAD(40007,"签名错误，请联系系统管理员"),

	;
	@Getter
	private final int code;
	@Getter
	private final String message;

	WebOfficeError(int code, String message) {
		this.code = code;
		this.message = message;
	}
}
