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
	@JsonProperty("locales")
	@NonNull
	private List<Map<String, Locale>> locales;

	public Locale getLocaleByLanguage(String name) {
		return locales.stream()
				.filter(e -> e.containsKey(name))
				.map(e -> e.get(name))
				.findFirst()
				.orElse(null);
	}
}
