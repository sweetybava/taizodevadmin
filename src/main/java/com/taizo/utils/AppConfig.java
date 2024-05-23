package com.taizo.utils;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSenderImpl;
@Configurable
public class AppConfig {
	
	@Bean
	public JavaMailSenderImpl javaMailSenderImpl(){
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("email-smtp.ap-south-1.amazonaws.com");
		mailSender.setPort(587);
		mailSender.setUsername("AKIAR34MTCMEFDQQZU32");
		mailSender.setPassword("BOyhEd+IzCPuoIrhkKQ1owt9BIl06qn6FCD3XLSF9Jvg");
		Properties prop = mailSender.getJavaMailProperties();
		prop.put("mail.transport.protocol", "smtp");
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.debug", "true");
		return mailSender;
	}
} 
