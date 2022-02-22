package chartgram.persistence.service;

import chartgram.persistence.entity.User;
import chartgram.persistence.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserService {

	private final UserRepository userRepository;

	@Autowired
	private UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> getAll() {
		List<User> result = new ArrayList<>();
		userRepository.findAll().forEach(result::add);
		return result;
	}

	public User add(User user) {
		Long id = user.getId();
		User persistedUser = userRepository.save(user);
		if (id == null) {
			log.info("Inserted user={} in DB", persistedUser);
		} else {
			log.info("Updated user={} in DB", persistedUser);
		}
		return persistedUser;
	}
}
