package chartgram.persistence;

import chartgram.persistence.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
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
}
