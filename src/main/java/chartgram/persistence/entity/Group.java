package chartgram.persistence.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity(name = "groups")
public class Group {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "id")
	private Long id;

	@Column(name="telegram_id", nullable = false)
	@NonNull
	private String telegramId;

	@Column(name="description")
	private String description;

	@Column(name="inserted_at", nullable = false)
	@NonNull
	private LocalDateTime insertedAt;

	public Group(@NonNull String telegramId, String description, @NonNull LocalDateTime insertedAt) {
		this.telegramId = telegramId;
		this.description = description;
		this.insertedAt = insertedAt;
	}
}
