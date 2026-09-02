package io.github.lijiajia3515.cairo.gateway.config;


import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;

@EnableDiscoveryClient
@Configuration(proxyBeanMethods = false)
public class CloudConfig {
}
