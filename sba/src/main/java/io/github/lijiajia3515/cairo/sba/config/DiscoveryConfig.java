package io.github.lijiajia3515.cairo.sba.config;


import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;

@EnableDiscoveryClient
@Configuration(proxyBeanMethods = false)
public class DiscoveryConfig {
}
