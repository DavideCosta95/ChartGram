package chartgram.persistence.repository;

import chartgram.persistence.entity.JoinEvent;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface JoinEventRepository extends CrudRepository<JoinEvent, Long> {
	List<JoinEvent> getAllByGroupTelegramId(String id);
}
