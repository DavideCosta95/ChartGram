package chartgram.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
public class ConfigurationFactory {

	@Bean
	public chartgram.config.Configuration configuration() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(new File("config/configuration.json"), chartgram.config.Configuration.class);
	}
}
