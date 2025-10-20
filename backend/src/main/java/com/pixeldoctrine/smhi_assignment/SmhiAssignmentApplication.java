package com.pixeldoctrine.smhi_assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmhiAssignmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmhiAssignmentApplication.class, args);
	}
}
