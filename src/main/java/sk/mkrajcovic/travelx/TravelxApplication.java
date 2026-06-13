package sk.mkrajcovic.travelx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class TravelxApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelxApplication.class, args);
	}

	@Bean
	ObjectMapper provideObjectMapper() {
		return new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

}
