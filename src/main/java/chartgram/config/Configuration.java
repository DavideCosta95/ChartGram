package chartgram.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@NoArgsConstructor
public class Configuration {
	@JsonProperty("language")
	@NonNull
	private String language;

	@JsonProperty("timezone")
	@NonNull
	private String timezone;

	@JsonProperty("bot")
	@NonNull
	private BotConfiguration botConfiguration;


	@Data
	@NoArgsConstructor
	public static class BotConfiguration {
		@JsonProperty("name")
		@NonNull
		private String name;

		@JsonProperty("username")
		@NonNull
		private String username;

		@JsonProperty("token")
		@NonNull
		private String token;

		@JsonProperty("start_command")
		@NonNull
		private String startCommand;

		@JsonProperty("help_command")
		@NonNull
		private String helpCommand;

		@JsonProperty("admin_panel_command")
		@NonNull
		private String adminPanelCommand;

		@JsonProperty("ignore_non_commands_messages")
		private Boolean ignoreNonCommandsMessages;

		@JsonProperty("developers_ids")
		@NonNull
		private List<Long> developersIds;

		@JsonProperty("owners_ids")
		@NonNull
		private List<Long> ownersIds;
	}
}