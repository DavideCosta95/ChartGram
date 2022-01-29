package chartgram.persistence.repository;

import chartgram.persistence.entity.LeaveEvent;
import org.springframework.data.repository.CrudRepository;

public interface LeaveEventRepository extends CrudRepository<LeaveEvent, Long> {
}
