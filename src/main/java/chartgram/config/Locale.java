package chartgram.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class Locale {
	@JsonProperty("non_command_text")
	@NonNull
	private String nonCommandText;

	@JsonProperty("private_command_not_allowed_text")
	@NonNull
	private String privateCommandNotAllowedText;

	@JsonProperty("unknown_command_text")
	@NonNull
	private String unknownCommandText;

	@JsonProperty("must_be_admin_text")
	@NonNull
	private String mustBeAdminText;

	@JsonProperty("link_sent_via_pvt_text")
	@NonNull
	private String linkSentViaPvtText;

	@JsonProperty("charts_sent_via_pvt_text")
	@NonNull
	private String chartsSentViaPvtText;
}
