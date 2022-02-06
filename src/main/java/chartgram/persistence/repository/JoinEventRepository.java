package chartgram.persistence.repository;

import chartgram.persistence.entity.JoinEvent;
import chartgram.persistence.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface JoinEventRepository extends CrudRepository<JoinEvent, Long> {
	JoinEvent findByJoiningUser(User addedUser);
}
