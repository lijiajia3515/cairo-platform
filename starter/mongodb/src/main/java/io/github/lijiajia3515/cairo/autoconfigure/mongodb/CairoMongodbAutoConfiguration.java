package io.github.lijiajia3515.cairo.autoconfigure.mongodb;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration(proxyBeanMethods = false)
public class CairoMongodbAutoConfiguration {

//	@Bean
//	@ConditionalOnMissingBean(MongoConverter.class)
//	MappingMongoConverter cairoMappingMongoConverter(MongoDatabaseFactory factory, MongoMappingContext context,
//													 MongoCustomConversions conversions) {
//		DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
//		MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);
//		mappingConverter.setCustomConversions(conversions);
//		mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null, context, mappingConverter::getWriteTarget));
//		return mappingConverter;
//	}
}
