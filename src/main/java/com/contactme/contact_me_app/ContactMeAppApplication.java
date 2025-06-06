package com.contactme.contact_me_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ContactMeAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContactMeAppApplication.class, args);
	}

}
