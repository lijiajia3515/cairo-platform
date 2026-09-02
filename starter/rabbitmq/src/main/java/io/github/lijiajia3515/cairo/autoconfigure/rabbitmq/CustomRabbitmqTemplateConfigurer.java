//package com.yr.cairo.rabbit;
//
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
//import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
//
//public class CustomRabbitmqTemplateConfigurer extends RabbitTemplateConfigurer {
//	private final AuthorizationMessagePostProcessor authorizationMessagePostProcessor;
//
//	public CustomRabbitmqTemplateConfigurer(RabbitProperties rabbitProperties, AuthorizationMessagePostProcessor authorizationMessagePostProcessor) {
//		super(rabbitProperties);
//		this.authorizationMessagePostProcessor = authorizationMessagePostProcessor;
//	}
//
//	@Override
//	public void configure(RabbitTemplate template, ConnectionFactory connectionFactory) {
//		super.configure(template, connectionFactory);
//		template.setBeforePublishPostProcessors(authorizationMessagePostProcessor);
//	}
//}
