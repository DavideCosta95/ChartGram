package chartgram.telegram;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class TelegramUser implements Serializable {
	@NonNull
	private Long id;

	private String firstName;
	private String lastName;
	private String nickname;

	public String getIdAsString() {
		return getId().toString();
	}

	public void setIdAsString(String id) {
		setId(Long.parseLong(id));
	}

	public String getPrettifiedUserData() {
		return ""
				+ (this.firstName == null ? "" : this.firstName)
				+ (this.lastName == null ? "" : " " + this.lastName)
				+ (this.nickname == null ? "" : " (@" + this.nickname + ")");
	}
}
