package chartgram.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class Language {
	@JsonProperty("non_command_text")
	@NonNull
	private String nonCommandText;
}
