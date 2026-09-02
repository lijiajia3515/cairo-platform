//package com.yr.cairo.autoconfigure.web;
//
//import com.yr.cairo.web.error.CairoErrorAttributes;
//import com.yr.cairo.web.error.CairoManagementErrorEndpoint;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
//import org.springframework.boot.actuate.autoconfigure.web.ManagementContextType;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
//import org.springframework.boot.autoconfigure.web.ServerProperties;
//import org.springframework.boot.web.servlet.error.ErrorAttributes;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Primary;
//
//@ManagementContextConfiguration(value = ManagementContextType.CHILD, proxyBeanMethods = false)
//public class CairoManagementWebConfiguration {
//
//	@Bean
//	@Primary
//	@ConditionalOnBean({ErrorAttributes.class, ServerProperties.class})
//	public CairoManagementErrorEndpoint cairoManagementErrorEndpoint(ErrorAttributes errorAttributes, ServerProperties serverProperties) {
//		return new CairoManagementErrorEndpoint(errorAttributes, serverProperties.getError());
//	}
//
//
//}
