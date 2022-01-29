package chartgram.api;

import chartgram.persistence.service.UserService;
import chartgram.persistence.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RestApiController {
	private final UserService userService;

	@Autowired
	private RestApiController(UserService userService) {
		this.userService = userService;
	}
	@GetMapping("/api/{id}")
	public User get(@PathVariable long id) {
		return userService.list().stream()
				.filter(e -> e.getId() == id)
				.findAny()
				.orElse(new User());
	}
}
