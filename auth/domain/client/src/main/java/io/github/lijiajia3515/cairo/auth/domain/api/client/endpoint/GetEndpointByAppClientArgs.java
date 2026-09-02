package io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 终端 查询 参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetEndpointByAppClientArgs {


	private List<EndpointInfo> EndpointInfos;

	@Data
	@Accessors(chain = true)
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class EndpointInfo{
		/**
		 * appId
		 */
		private String appId;


		/**
		 * 终端ID
		 */
		private String endpointId;
	}

}
