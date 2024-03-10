package com.umi.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

@Configuration
public class ExceptionConfiguration {

	@Bean
	public SimpleMappingExceptionResolver resolver() {
		SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver();
		
		Properties mappings = new Properties();
		mappings.put("java.lang.Exception", "/500.html");
		resolver.setExceptionMappings(mappings );
		
		return resolver;
	}
}
