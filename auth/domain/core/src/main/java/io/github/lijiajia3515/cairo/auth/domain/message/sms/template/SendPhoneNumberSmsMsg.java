package io.github.lijiajia3515.cairo.auth.domain.message.sms.template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendPhoneNumberSmsMsg {

    /**
     * 手机号
     */
    @NotNull
    private String phoneNumber;

	/**
	 * 应用ID
	 */
	@NotNull
	private String appId;

    /**
     * 签名
     */
    private String sign;

    /**
     * 业务ID
     */
    @NotNull
    private String bizId;


    /**
     * 参数
     */
    private Map<String, String> params;
}
