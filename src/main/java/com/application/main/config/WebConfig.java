package com.application.main.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
@Configuration	
//@EnableSpringDataWebSupport(pageSerializationMode = PagedResourcesAssembler.PageSerializationMode.VIA_COLLECTION_RESOURCE_WITH_PAGING)
public class WebConfig {
	
	@Bean
	public SecurityFilterChain filter(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.authorizeHttpRequests().requestMatchers("/actuator/**").permitAll().anyRequest().authenticated().and()
        .oauth2ResourceServer()
        .jwt();
		http.cors();
		return http.build();
	}

}
