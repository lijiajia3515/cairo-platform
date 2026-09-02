package io.github.lijiajia3515.cairo.auth.config;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.mongodb.serial.SerialService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
public class SerialConfig {
	@Bean
	public SerialService serialService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate, TransactionTemplate transactionTemplate) {
		return new SerialService(MongodbConstants.Collection.SERIAL, transactionTemplate, mongoTemplate);
	}
}
