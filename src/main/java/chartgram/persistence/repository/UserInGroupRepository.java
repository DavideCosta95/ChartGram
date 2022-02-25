package chartgram.persistence.repository;

import chartgram.persistence.entity.UserInGroup;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserInGroupRepository extends CrudRepository<UserInGroup, UserInGroup.UserInGroupKey> {
	List<UserInGroup> getAllByGroupTelegramId(String id);
	List<UserInGroup> getAllByUserTelegramId(String id);
}
