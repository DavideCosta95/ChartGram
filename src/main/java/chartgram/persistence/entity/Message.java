package chartgram.persistence.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity(name = "messages")
public class Message {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@NonNull
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
}
