package nik.nkochnev.io.botForNik;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BotForNikApplication {

	public static void main(String[] args) {
		SpringApplication.run(BotForNikApplication.class, args);
	}

}
