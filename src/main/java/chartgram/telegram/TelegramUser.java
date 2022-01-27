package chartgram.telegram;

import lombok.Data;

import java.io.Serializable;

@Data
public class TelegramUser implements Serializable {
	private Long id;
	private String firstName;
	private String lastName;
	private String nickname;

	public TelegramUser(Long id, String firstName, String lastName, String nickname) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.nickname = nickname;
	}

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
