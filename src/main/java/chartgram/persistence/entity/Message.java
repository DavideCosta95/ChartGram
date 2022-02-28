package chartgram.persistence.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity(name = "messages")
public class Message implements TemporalEvent {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "id")
	private Long id;

	@Column(name="sent_at", nullable = false)
	@NonNull
	private LocalDateTime sentAt;

	@ManyToOne
	@JoinColumn(name = "sender_id", nullable=false)
	private User sender;

	@ManyToOne
	@JoinColumn(name = "group_id", nullable=false)
	private Group group;

	@Column(name="text")
	private String text;

	@Column(name = "type", nullable = false)
	private Integer type;

	public Message(@NonNull LocalDateTime sentAt, User sender, Group group, String text, Integer type) {
		this.sentAt = sentAt;
		this.sender = sender;
		this.group = group;
		this.text = text;
		this.type = type;
	}

	@Override
	public LocalDateTime getAt() {
		return getSentAt();
	}
}
