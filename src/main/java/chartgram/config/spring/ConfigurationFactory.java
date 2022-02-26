package chartgram.config.spring;

import chartgram.config.Localization;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
public class ConfigurationFactory {
	@Value("${server.port}")
	private Integer webappPort;

	@Bean
	public chartgram.config.Configuration configuration() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		chartgram.config.Configuration configuration = objectMapper.readValue(new File("config/configuration.json"), chartgram.config.Configuration.class);
		configuration.getWebappConfiguration().setPort(webappPort);
		return configuration;
	}

	@Bean
	public Localization localization() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(new File("config/localization.json"), Localization.class);
	}
}
