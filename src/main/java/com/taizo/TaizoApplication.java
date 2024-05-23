package com.taizo;

import javax.sql.DataSource;

//
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
@EnableAsync
@SpringBootApplication(scanBasePackages ="com.taizo")
@EnableScheduling
@Configuration
public class TaizoApplication extends SpringBootServletInitializer {

    @Bean(name="processExecutor")
    public TaskExecutor workExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("Async-");
        threadPoolTaskExecutor.setCorePoolSize(3);
        threadPoolTaskExecutor.setMaxPoolSize(3);
        threadPoolTaskExecutor.setQueueCapacity(600);
        threadPoolTaskExecutor.afterPropertiesSet();
        return threadPoolTaskExecutor;
    }

    public static void main(String[] args) {
        SpringApplication.run(TaizoApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(TaizoApplication.class);
    }
    
    

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    
}
