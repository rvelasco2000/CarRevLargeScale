package it.unipi.CarRev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarRevApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarRevApplication.class, args);
	}

}
