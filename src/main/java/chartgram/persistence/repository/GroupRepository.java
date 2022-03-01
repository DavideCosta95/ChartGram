package chartgram.persistence.repository;

import chartgram.persistence.entity.Group;
import org.springframework.data.repository.CrudRepository;

public interface GroupRepository extends CrudRepository<Group, Long> {
	Group getGroupByTelegramId(String telegramId);
	Group findFirstByIdAfter(long id);
}
