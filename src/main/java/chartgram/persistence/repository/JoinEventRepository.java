package chartgram.persistence.repository;

import chartgram.persistence.entity.JoinEvent;
import org.springframework.data.repository.CrudRepository;

public interface JoinEventRepository extends CrudRepository<JoinEvent, Long> {
}
