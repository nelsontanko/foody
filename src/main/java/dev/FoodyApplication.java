package dev;

import dev.core.config.FoodyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({FoodyProperties.class})
public class FoodyApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodyApplication.class, args);
	}

}
