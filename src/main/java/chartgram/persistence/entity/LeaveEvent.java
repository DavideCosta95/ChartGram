package chartgram.persistence.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity(name = "leave_events")
public class LeaveEvent implements TemporalEvent {
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
	private User leavingUser;

	@ManyToOne
	@JoinColumn(name = "remover_user_id", nullable=true)
	private User removerUser;

	@ManyToOne
	@JoinColumn(name = "group_id", nullable=false)
	@NonNull
	private Group group;

	@Override
	public LocalDateTime getAt() {
		return getLeavingAt();
	}
}
