package io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一返回值
 * @param <T>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebofficeResult<T> implements Serializable {
	/**
	 * code
	 */
	@JsonProperty("code")
	private Integer code;

	/**
	 * message
	 */
	@JsonProperty("message")
	private String message;

	@JsonProperty("data")
	private T data;
}
