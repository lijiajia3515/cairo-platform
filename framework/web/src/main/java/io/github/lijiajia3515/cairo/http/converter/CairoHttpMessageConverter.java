package io.github.lijiajia3515.cairo.http.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;

public class CairoHttpMessageConverter extends AbstractJackson2HttpMessageConverter {

	public CairoHttpMessageConverter(ObjectMapper objectMapper) {
		super(objectMapper, new MediaType("application", "vnd.cairo.*+json"));
	}

	public CairoHttpMessageConverter(ObjectMapper objectMapper, MediaType... supportedMediaTypes) {
		super(objectMapper, supportedMediaTypes);
	}


}
