package chartgram.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class Configuration {
	@JsonProperty("name")
	@NonNull
	private String name;
}