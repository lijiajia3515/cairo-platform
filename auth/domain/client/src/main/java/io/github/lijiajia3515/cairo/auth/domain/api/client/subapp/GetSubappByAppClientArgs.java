package io.github.lijiajia3515.cairo.auth.domain.api.client.subapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 查询 子应用 参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetSubappByAppClientArgs {


	private List<SubappInfo> subappInfos;

	@Data
	@Accessors(chain = true)
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class SubappInfo {
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
