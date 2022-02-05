package chartgram.persistence.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity(name = "users")
public class User {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@NonNull
	@Id
	@Column(name = "id")
	private Long id;

	@Column(name="telegram_id", nullable = false)
	@NonNull
	private String telegramId;

	@Column(name="telegram_first_name")
	private String telegramFirstName;

	@Column(name="telegram_last_name")
	private String telegramLastName;

	@Column(name="telegram_username")
	private String telegramUsername;

	@Column(name="inserted_at", nullable = false)
	@NonNull
	private LocalDateTime insertedAt;

	public User(@NonNull String telegramId, String telegramFirstName, String telegramLastName, String telegramUsername, @NonNull LocalDateTime insertedAt) {
		this.telegramId = telegramId;
		this.telegramFirstName = telegramFirstName;
		this.telegramLastName = telegramLastName;
		this.telegramUsername = telegramUsername;
		this.insertedAt = insertedAt;
	}
}
