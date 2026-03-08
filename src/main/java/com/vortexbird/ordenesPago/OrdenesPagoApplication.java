package com.vortexbird.ordenesPago;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OrdenesPagoApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrdenesPagoApplication.class, args);
	}

}
