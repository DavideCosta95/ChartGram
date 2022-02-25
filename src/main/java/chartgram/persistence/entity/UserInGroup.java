package chartgram.persistence.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Entity(name = "users_in_groups")
public class UserInGroup {
	@EmbeddedId
	private UserInGroupKey userInGroupKey = new UserInGroupKey();

	@ManyToOne
	@NonNull
	@JoinColumn(name = "user_id", insertable = false, updatable = false)
	@MapsId("userId")
	private User user;

	@ManyToOne
	@NonNull
	@JoinColumn(name = "group_id", insertable = false, updatable = false)
	@MapsId("groupId")
	private Group group;

	@Data
	@NoArgsConstructor
	@Embeddable
	public static class UserInGroupKey implements Serializable {
		@Column(name = "user_id")
		private Long userId;

		@Column(name = "group_id")
		private Long groupId;
	}
}
