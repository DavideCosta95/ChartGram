package chartgram.persistence.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@Entity(name = "users")
public class User {
	@GeneratedValue
	@NonNull
	@Id
	@Column(name = "id")
	private Long id;

	@Column(name="name")
	@NonNull
	private String name;
}
