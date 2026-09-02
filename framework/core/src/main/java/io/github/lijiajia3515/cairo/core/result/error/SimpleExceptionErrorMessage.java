package io.github.lijiajia3515.cairo.core.result.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 异常消息
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleExceptionErrorMessage {
	private String bean;
	private String message;

}
