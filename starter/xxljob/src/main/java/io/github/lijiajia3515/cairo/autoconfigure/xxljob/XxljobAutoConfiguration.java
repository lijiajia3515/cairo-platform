package io.github.lijiajia3515.cairo.autoconfigure.xxljob;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import io.github.lijiajia3515.cairo.xxljob.XxljobProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * xxljob 自动配置类
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(UtilAutoConfiguration.class)
public class XxljobAutoConfiguration {
	@Bean
	@ConfigurationProperties(prefix = "cairo.xxljob")
	public XxljobProperties xxljobProperties() {
		return new XxljobProperties();
	}

	@Bean
	@ConditionalOnBean({XxljobProperties.class, InetUtils.class})
	public XxlJobSpringExecutor xxlJobExecutor(XxljobProperties properties, InetUtils inetUtils, @Value("${spring.application.name:xxljob-executor}") String defaultApplicationName) {

		String appname = Optional.ofNullable(properties.getExecutor().getAppname()).filter(x -> !x.isBlank()).orElse(defaultApplicationName);
		String ip = inetUtils.findFirstNonLoopbackAddress().getHostAddress();


		XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
		xxlJobSpringExecutor.setAdminAddresses(properties.getAdminAddress());
		xxlJobSpringExecutor.setAccessToken(properties.getAccessToken());
		xxlJobSpringExecutor.setAppname(appname);

		xxlJobSpringExecutor.setPort(properties.getExecutor().getPort());
		xxlJobSpringExecutor.setIp(inetUtils.findFirstNonLoopbackAddress().getHostAddress());
		xxlJobSpringExecutor.setLogPath(properties.getExecutor().getLogPath());
		xxlJobSpringExecutor.setLogRetentionDays(properties.getExecutor().getLogRetentionDays());
		log.info(">>>>>>>>>>> xxl-job config init. adminAddress: {} appname: {} host: {} port: {} ", properties.getAdminAddress(), appname, ip, properties.getExecutor().getPort());
		return xxlJobSpringExecutor;
	}


}
