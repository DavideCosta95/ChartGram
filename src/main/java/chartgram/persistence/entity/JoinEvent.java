package chartgram.persistence.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity(name = "join_events")
public class JoinEvent {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@NonNull
	@Id
	@Column(name = "id")
	private Long id;

	@Column(name="joined_at", nullable = false)
	@NonNull
	private LocalDateTime joinedAt;

	@ManyToOne
	@JoinColumn(name = "joining_user_id", nullable=false)
	@NonNull
	private User joiningUser;

	@ManyToOne
	@JoinColumn(name = "adder_user_id", nullable=true)
	private User adderUser;
}
