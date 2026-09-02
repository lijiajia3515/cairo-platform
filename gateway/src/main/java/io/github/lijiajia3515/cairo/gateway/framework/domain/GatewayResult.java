package io.github.lijiajia3515.cairo.gateway.framework.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayResult<T> {

	@JsonProperty("Code")
	private String code;

	@JsonProperty("Message")
	private String message;

	@JsonProperty("Data")
	private T data;

}
