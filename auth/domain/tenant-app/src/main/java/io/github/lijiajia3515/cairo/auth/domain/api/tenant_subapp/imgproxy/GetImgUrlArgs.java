package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.imgproxy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetImgUrlArgs implements Serializable {
	/**
	 * 原地址
	 */
	private String sourceUrl;
	/**
	 * 参数
	 */
	private Map<String, String> params;
}
