package chartgram.persistence.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity(name = "users_in_groups")
@IdClass(UserInGroup.UserInGroupKey.class)
public class UserInGroup {
	@Id
	@NonNull
	@JoinColumn(name = "user_id", nullable=false)
	@ManyToOne
	private User user;

	@Id
	@NonNull
	@JoinColumn(name = "group_id", nullable=false)
	@ManyToOne
	private Group group;

	public static class UserInGroupKey implements Serializable {
		private User user;
		private Group group;
	}
}
