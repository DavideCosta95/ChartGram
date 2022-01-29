package chartgram.persistence.service;

import chartgram.persistence.entity.User;
import chartgram.persistence.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService extends GenericService<User> {

	@Autowired
	private UserService(UserRepository userRepository) {
		super(userRepository);
	}
}
