package chartgram.persistence.repository;

import chartgram.persistence.entity.Message;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MessageRepository extends CrudRepository<Message, Long> {
	List<Message> getAllByGroupTelegramId(String id);
}
