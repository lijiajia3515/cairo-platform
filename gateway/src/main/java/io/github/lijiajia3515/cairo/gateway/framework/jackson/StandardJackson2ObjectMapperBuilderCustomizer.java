package io.github.lijiajia3515.cairo.gateway.framework.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public final class StandardJackson2ObjectMapperBuilderCustomizer
	implements Jackson2ObjectMapperBuilderCustomizer, Ordered {
	private static final Map<?, Boolean> FEATURE_DEFAULTS;

	static {
		Map<Object, Boolean> featureDefaults = new HashMap<>();
		featureDefaults.put(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		featureDefaults.put(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
		FEATURE_DEFAULTS = Collections.unmodifiableMap(featureDefaults);
	}

	private final ApplicationContext applicationContext;

	private final CairoJacksonProperties jacksonProperties;

	public StandardJackson2ObjectMapperBuilderCustomizer(ApplicationContext applicationContext,
														 CairoJacksonProperties jacksonProperties) {
		this.applicationContext = applicationContext;
		this.jacksonProperties = jacksonProperties;
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public void customize(Jackson2ObjectMapperBuilder builder) {
		if (this.jacksonProperties.getDefaultPropertyInclusion() != null) {
			builder.serializationInclusion(this.jacksonProperties.getDefaultPropertyInclusion());
		}
		if (this.jacksonProperties.getTimeZone() != null) {
			builder.timeZone(this.jacksonProperties.getTimeZone());
		}
		configureFeatures(builder, FEATURE_DEFAULTS);
		configureVisibility(builder, this.jacksonProperties.getVisibility());
		configureFeatures(builder, this.jacksonProperties.getDeserialization());
		configureFeatures(builder, this.jacksonProperties.getSerialization());
		configureFeatures(builder, this.jacksonProperties.getMapper());
		configureFeatures(builder, this.jacksonProperties.getParser());
		configureFeatures(builder, this.jacksonProperties.getGenerator());
		configureDateFormat(builder);
		configurePropertyNamingStrategy(builder);
		configureModules(builder);
		configureLocale(builder);
	}

	private void configureFeatures(Jackson2ObjectMapperBuilder builder, Map<?, Boolean> features) {
		features.forEach((feature, value) -> {
			if (value != null) {
				if (value) {
					builder.featuresToEnable(feature);
				} else {
					builder.featuresToDisable(feature);
				}
			}
		});
	}

	private void configureVisibility(Jackson2ObjectMapperBuilder builder,
									 Map<PropertyAccessor, JsonAutoDetect.Visibility> visibilities) {
		visibilities.forEach(builder::visibility);
	}

	private void configureDateFormat(Jackson2ObjectMapperBuilder builder) {
		// We support a fully qualified class name extending DateFormat or a date
		// pattern string value
		String dateFormat = this.jacksonProperties.getDateFormat();
		if (dateFormat != null) {
			try {
				Class<?> dateFormatClass = ClassUtils.forName(dateFormat, null);
				builder.dateFormat((DateFormat) BeanUtils.instantiateClass(dateFormatClass));
			} catch (ClassNotFoundException ex) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
				// Since Jackson 2.6.3 we always need to set a TimeZone (see
				// gh-4170). If none in our properties fallback to the Jackson's
				// default
				TimeZone timeZone = this.jacksonProperties.getTimeZone();
				if (timeZone == null) {
					timeZone = new ObjectMapper().getSerializationConfig().getTimeZone();
				}
				simpleDateFormat.setTimeZone(timeZone);
				builder.dateFormat(simpleDateFormat);
			}
		}
	}

	private void configurePropertyNamingStrategy(Jackson2ObjectMapperBuilder builder) {
		// We support a fully qualified class name extending Jackson's
		// PropertyNamingStrategy or a string value corresponding to the constant
		// names in PropertyNamingStrategy which hold default provided
		// implementations
		String strategy = this.jacksonProperties.getPropertyNamingStrategy();
		if (strategy != null) {
			try {
				configurePropertyNamingStrategyClass(builder, ClassUtils.forName(strategy, null));
			} catch (ClassNotFoundException ex) {
				configurePropertyNamingStrategyField(builder, strategy);
			}
		}
	}

	private void configurePropertyNamingStrategyClass(Jackson2ObjectMapperBuilder builder,
													  Class<?> propertyNamingStrategyClass) {
		builder.propertyNamingStrategy(
			(PropertyNamingStrategy) BeanUtils.instantiateClass(propertyNamingStrategyClass));
	}

	private void configurePropertyNamingStrategyField(Jackson2ObjectMapperBuilder builder, String fieldName) {
		// Find the field (this way we automatically support new constants
		// that may be added by Jackson in the future)
		Field field = findPropertyNamingStrategyField(fieldName);
		Assert.notNull(field, () -> "Constant named '" + fieldName + "' not found");
		try {
			builder.propertyNamingStrategy((PropertyNamingStrategy) field.get(null));
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private Field findPropertyNamingStrategyField(String fieldName) {
		try {
			return ReflectionUtils.findField(com.fasterxml.jackson.databind.PropertyNamingStrategies.class,
				fieldName, PropertyNamingStrategy.class);
		} catch (NoClassDefFoundError ex) { // Fallback pre Jackson 2.12
			return ReflectionUtils.findField(PropertyNamingStrategy.class, fieldName,
				PropertyNamingStrategy.class);
		}
	}

	private void configureModules(Jackson2ObjectMapperBuilder builder) {
		Collection<Module> moduleBeans = getBeans(this.applicationContext, Module.class);
		builder.modulesToInstall(moduleBeans.toArray(new Module[0]));
	}

	private void configureLocale(Jackson2ObjectMapperBuilder builder) {
		Locale locale = this.jacksonProperties.getLocale();
		if (locale != null) {
			builder.locale(locale);
		}
	}

	private static <T> Collection<T> getBeans(ListableBeanFactory beanFactory, Class<T> type) {
		return BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, type).values();
	}

}
