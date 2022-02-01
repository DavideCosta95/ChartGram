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

	public List<User> list() {
		List<User> result = new ArrayList<>();
		userRepository.findAll().forEach(result::add);
		return result;
	}

	public User add(User user) {
		User alreadyPersistedUser = userRepository.findByTelegramId(user.getTelegramId());
		if (alreadyPersistedUser == null) {
			log.info("Inserted user={} in DB", user);
			return userRepository.save(user);
		}
		log.debug("User already present in DB: user={}", user);
		return alreadyPersistedUser;
	}
}
