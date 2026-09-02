package io.github.lijiajia3515.cairo.xxljob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * xxl 配置文件
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XxljobProperties {
	/**
	 * xxl-job 地址
	 */
	@Builder.Default
	private String adminAddress = "http://xxljob-server";

	/**
	 * xxl-job token
	 */
	@Builder.Default
	private String accessToken = "";

	/**
	 * 执行器配置
	 */
	@Builder.Default
	public Executor executor = new Executor();

	/**
	 * xxl执行器配置
	 */
	@Data
	@Accessors(chain = true)
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Executor {

		/**
		 * 执行器名称
		 */
		private String appname;

		/**
		 * 暴露执行器的端口
		 */
		@Builder.Default
		private int port = -1;

		/**
		 * 日志路径
		 */
		@Builder.Default
		private String logPath = "xxljob";

		/**
		 * 日志滚动周期
		 */
		@Builder.Default
		private int logRetentionDays = 30;
	}
}
