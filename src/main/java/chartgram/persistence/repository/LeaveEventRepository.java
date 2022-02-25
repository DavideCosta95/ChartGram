package chartgram.persistence.repository;

import chartgram.persistence.entity.LeaveEvent;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LeaveEventRepository extends CrudRepository<LeaveEvent, Long> {
	List<LeaveEvent> getAllByGroupTelegramId(String id);
}
