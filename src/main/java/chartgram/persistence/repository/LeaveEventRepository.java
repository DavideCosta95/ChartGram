package chartgram.persistence.repository;

import chartgram.persistence.entity.LeaveEvent;
import chartgram.persistence.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface LeaveEventRepository extends CrudRepository<LeaveEvent, Long> {
	LeaveEvent findByLeavingUser(User leavingUser);
}
