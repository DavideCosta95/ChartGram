package chartgram.persistence.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity(name = "leave_events")
public class LeaveEvent {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@NonNull
	@Id
	@Column(name = "id")
	private Long id;

	@Column(name="leaving_at", nullable = false)
	@NonNull
	private LocalDateTime leavingAt;

	@ManyToOne
	@JoinColumn(name = "leaving_user_id", nullable=false)
	@NonNull
	private User leavingUserId;

	@ManyToOne
	@JoinColumn(name = "remover_user_id", nullable=true)
	private User removerUserId;
}
