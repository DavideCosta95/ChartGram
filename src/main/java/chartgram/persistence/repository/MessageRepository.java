package chartgram.persistence.repository;

import chartgram.persistence.entity.Message;
import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<Message, Long> {
}
