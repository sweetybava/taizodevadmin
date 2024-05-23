package com.taizo.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

public class DataSourceConfig2 {

	 @Bean(name = "secondDataSource")
	    @ConfigurationProperties(prefix = "spring.datasource.second")
	    public DataSource secondDataSource(DataSourceProperties properties) {
	        // Use the actual property values without quotes
	        return DataSourceBuilder.create()
	                .url(properties.determineUrl()) 
	                .username(properties.determineUsername()) 
	                .password(properties.determinePassword()) 
	                .driverClassName(properties.getDriverClassName()) 
	                .build();
	    }

}
