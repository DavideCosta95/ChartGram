package chartgram.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class Localization {
	@JsonProperty("languages")
	@NonNull
	private List<Map<String, Language>> languages;

	public Language getLanguage(String name) {
		return languages.stream()
				.filter(e -> e.containsKey(name))
				.map(e -> e.get(name))
				.findFirst()
				.orElse(null);
	}
}
