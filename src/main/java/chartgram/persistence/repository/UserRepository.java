package chartgram.persistence.repository;

import chartgram.persistence.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
