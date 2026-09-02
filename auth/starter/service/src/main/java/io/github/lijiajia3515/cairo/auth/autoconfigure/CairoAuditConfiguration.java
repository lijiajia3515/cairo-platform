package io.github.lijiajia3515.cairo.auth.autoconfigure;

import io.github.lijiajia3515.cairo.auth.framework.audit.CairoAuditorWare;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * 审核注入 配置类
 */
@Configuration(proxyBeanMethods = false)
@EnableMongoAuditing
public class CairoAuditConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnClass(CairoAuditorWare.class)
	AuditorAware<String> cairoAuditorWare() {
		return new CairoAuditorWare();
	}

}
