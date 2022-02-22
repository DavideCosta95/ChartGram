package chartgram.api;

import chartgram.persistence.entity.*;
import chartgram.persistence.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class RestApiController {
	private final UserService userService;
	private final MessageService messageService;
	private final JoinEventService joinEventService;
	private final LeaveEventService leaveEventService;
	private final GroupService groupService;

	@Autowired
	private RestApiController(UserService userService, MessageService messageService, JoinEventService joinEventService, LeaveEventService leaveEventService, GroupService groupService) {
		this.userService = userService;
		this.messageService = messageService;
		this.joinEventService = joinEventService;
		this.leaveEventService = leaveEventService;
		this.groupService = groupService;
	}

	@GetMapping("/api/{id}")
	public User get(@PathVariable long id) {
		return userService.getAll().stream()
				.filter(e -> e.getId() == id)
				.findAny()
				.orElse(new User());
	}

	@GetMapping("/api/messages")
	public List<Message> getAllMessages() {
		return messageService.getAll();
	}

	@GetMapping("/api/join-events")
	public List<JoinEvent> getAllJoinEvents() {
		return joinEventService.getAll();
	}

	@GetMapping("/api/leave-events")
	public List<LeaveEvent> getAllLeaveEvents() {
		return leaveEventService.getAll();
	}

	@GetMapping("/api/groups")
	public List<Group> getAllGroups() {
		return groupService.getAll();
	}

	@GetMapping("/api/users")
	public List<User> getAllUsers() {
		return userService.getAll();
	}
}
