
package com.runner;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

import java.util.logging.Logger;

@SpringBootApplication
public class SampleSpringBootApp extends SpringBootServletInitializer {
	private Logger logger = Logger.getLogger("SampleSpringBootApp");


	public static void main(String[] args) throws Exception {
		SpringApplication.run(SampleSpringBootApp.class, args);
	}


}
