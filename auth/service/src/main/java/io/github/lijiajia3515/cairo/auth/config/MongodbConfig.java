package io.github.lijiajia3515.cairo.auth.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import io.github.lijiajia3515.cairo.auth.framework.audit.CairoAuditorWare;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.data.mongo.MongoHealthIndicator;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.boot.autoconfigure.mongo.MongoClientFactory;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.mongo.PropertiesMongoConnectionDetails;
import org.springframework.boot.autoconfigure.mongo.StandardMongoClientSettingsBuilderCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoManagedTypes;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoDatabaseFactorySupport;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
@EnableMongoAuditing
public class MongodbConfig {

	@Bean
	CairoAuditorWare cairoAuditorWare() {
		return new CairoAuditorWare();
	}

	@Bean
	MongoCustomConversions mongodbCustomConversions() {
		return new MongoCustomConversions(Collections.emptyList());
	}

	@Bean
	MongoClientSettings mongodbClientSettings() {
		return MongoClientSettings.builder().build();
	}

	@Bean
	@ConfigurationProperties(prefix = "mongodb.sources.master")
	@Primary
	public MongoProperties masterMongodbProperties() {
		return new MongoProperties();
	}

	@Bean
	@Primary
	public PropertiesMongoConnectionDetails masterMongodbConnectionDetails(@Qualifier("masterMongodbProperties") MongoProperties properties,
																			ObjectProvider<SslBundles> sslBundles) {
		return new PropertiesMongoConnectionDetails(properties, sslBundles.getIfAvailable());
	}


	StandardMongoClientSettingsBuilderCustomizer mongodbPropertiesCustomizer(MongoProperties properties,
																			 PropertiesMongoConnectionDetails connectionDetails) {
		return new StandardMongoClientSettingsBuilderCustomizer(connectionDetails,
			properties.getUuidRepresentation());
	}


	@Bean
	MongoMappingContext mongoMappingContext(@Qualifier("masterMongodbProperties") MongoProperties properties,
											MongoCustomConversions conversions,
											MongoManagedTypes managedTypes) {
		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		MongoMappingContext context = new MongoMappingContext();
		map.from(properties.isAutoIndexCreation()).to(context::setAutoIndexCreation);
		context.setManagedTypes(managedTypes);
		Class<?> strategyClass = properties.getFieldNamingStrategy();
		if (strategyClass != null) {
			context.setFieldNamingStrategy((FieldNamingStrategy) BeanUtils.instantiateClass(strategyClass));
		}
		context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
		return context;
	}

	@Bean
	@Primary
	public MongoClient masterMongoClient(ObjectProvider<MongoClientSettingsBuilderCustomizer> builderCustomizers,
										 @Qualifier("masterMongodbProperties") MongoProperties properties,
										 @Qualifier("masterMongodbConnectionDetails") PropertiesMongoConnectionDetails connectionDetails,
										 ObjectProvider<SslBundles> sslBundles,
										 @Qualifier("mongodbClientSettings") MongoClientSettings mongodbClientSettings) {
		List<MongoClientSettingsBuilderCustomizer> collect = builderCustomizers.orderedStream().collect(Collectors.toList());
		collect.add(mongodbPropertiesCustomizer(properties, connectionDetails));
		return new MongoClientFactory(collect).createMongoClient(mongodbClientSettings);
	}

	@Bean
	@Primary
	MongoDatabaseFactorySupport<?> masterMongodbFactory(@Qualifier("masterMongoClient") MongoClient mongoClient,
														@Qualifier("masterMongodbProperties") MongoProperties properties) {
		return new SimpleMongoClientDatabaseFactory(mongoClient, properties.getMongoClientDatabase());
	}


	@Bean
	@Primary
	MappingMongoConverter mongoMappingMongoConverter(@Qualifier("masterMongodbFactory") MongoDatabaseFactory factory,
													 @Qualifier("mongoMappingContext") MongoMappingContext context,
													 @Qualifier("mongodbCustomConversions") MongoCustomConversions conversions) {
		DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
		MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);
		mappingConverter.setCustomConversions(conversions);
		mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null, context, mappingConverter::getWriteTarget));
		return mappingConverter;
	}

	@Bean
	@Primary
	MongoTemplate mongoTemplate(@Qualifier("masterMongodbFactory") MongoDatabaseFactory mongoDatabaseFactory,
								@Qualifier("mongoMappingMongoConverter") MappingMongoConverter converter) {
		return new MongoTemplate(mongoDatabaseFactory, converter);
	}


	@Bean
	MongoHealthIndicator mongodbHealthIndicator(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate) {
		return new MongoHealthIndicator(mongoTemplate);
	}

	@Bean
	MongoTransactionManager mongoTransactionManager(@Qualifier("masterMongodbFactory") MongoDatabaseFactory mongoDatabaseFactory) {
		return new MongoTransactionManager(mongoDatabaseFactory);
	}


	@Bean
	MongoMappingContext readMongodbMappingContext(ApplicationContext applicationContext,
												  @Qualifier("readMongodbProperties") MongoProperties properties,
												  @Qualifier("mongodbCustomConversions") MongoCustomConversions conversions)
		throws ClassNotFoundException {
		PropertyMapper mapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
		MongoMappingContext context = new MongoMappingContext();
		mapper.from(properties.isAutoIndexCreation()).to(context::setAutoIndexCreation);
		context.setInitialEntitySet(new EntityScanner(applicationContext).scan(Document.class));
		Class<?> strategyClass = properties.getFieldNamingStrategy();
		if (strategyClass != null) {
			context.setFieldNamingStrategy((FieldNamingStrategy) BeanUtils.instantiateClass(strategyClass));
		}
		context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
		return context;
	}

	@Bean
	@ConfigurationProperties(prefix = "mongodb.sources.read")
	public MongoProperties readMongodbProperties() {
		return new MongoProperties();
	}

	@Bean
	public PropertiesMongoConnectionDetails readMongodbConnectionDetails(@Qualifier("readMongodbProperties") MongoProperties properties,
																		 ObjectProvider<SslBundles> sslBundles) {
		return new PropertiesMongoConnectionDetails(properties, sslBundles.getIfAvailable());
	}


	@Bean
	public MongoClient readMongodbClient(ObjectProvider<MongoClientSettingsBuilderCustomizer> builderCustomizers,
										 @Qualifier("readMongodbProperties") MongoProperties properties,
										 @Qualifier("readMongodbConnectionDetails") PropertiesMongoConnectionDetails connectionDetails,
										 ObjectProvider<SslBundles> sslBundles,
										 @Qualifier("mongodbClientSettings") MongoClientSettings readMongoClientSettings) {
		List<MongoClientSettingsBuilderCustomizer> collect = builderCustomizers.orderedStream().collect(Collectors.toList());
		collect.add(mongodbPropertiesCustomizer(properties, connectionDetails));
		return new MongoClientFactory(collect).createMongoClient(readMongoClientSettings);
	}


	@Bean
	MongoDatabaseFactorySupport<?> readMongodbFactory(@Qualifier("readMongodbClient") MongoClient mongoClient,
													  @Qualifier("readMongodbProperties") MongoProperties properties) {
		return new SimpleMongoClientDatabaseFactory(mongoClient, properties.getMongoClientDatabase());
	}

	@Bean
	MappingMongoConverter readMappingMongodbConverter(@Qualifier("readMongodbFactory") MongoDatabaseFactory factory,
													  @Qualifier("readMongodbMappingContext") MongoMappingContext context,
													  @Qualifier("mongodbCustomConversions") MongoCustomConversions conversions) {
		DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
		MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);
		mappingConverter.setCustomConversions(conversions);
		mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null, context, mappingConverter::getWriteTarget));
		return mappingConverter;
	}


	@Bean
	MongoTemplate readMongoTemplate(@Qualifier("readMongodbFactory") MongoDatabaseFactory mongoDatabaseFactory,
									@Qualifier("readMappingMongodbConverter") MappingMongoConverter converter) {
		return new MongoTemplate(mongoDatabaseFactory, converter);
	}

	@Bean
	MongoHealthIndicator readMongodbHealthIndicator(@Qualifier("readMongoTemplate") MongoTemplate mongoTemplate) {
		return new MongoHealthIndicator(mongoTemplate);
	}

}
