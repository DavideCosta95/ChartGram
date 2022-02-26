package chartgram.api;

import chartgram.config.Configuration;
import chartgram.persistence.entity.*;
import chartgram.persistence.service.*;
import chartgram.telegram.TelegramController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/groups")
public class RestApiController {
	private final UserInGroupService userInGroupService;
	private final MessageService messageService;
	private final JoinEventService joinEventService;
	private final LeaveEventService leaveEventService;
	private final TelegramController telegramController;

	private final boolean test;

	@Autowired
	private RestApiController(Configuration configuration, UserInGroupService userInGroupService, MessageService messageService, JoinEventService joinEventService, LeaveEventService leaveEventService, TelegramController telegramController) {
		this.userInGroupService = userInGroupService;
		this.messageService = messageService;
		this.joinEventService = joinEventService;
		this.leaveEventService = leaveEventService;
		this.telegramController = telegramController;
		this.test = configuration.isTest();
	}

	@ModelAttribute
	public void addAttributes(@RequestHeader(value = "authorization", required = false) String authorizationHeader, Model model, HttpServletRequest request, HttpServletResponse response) {
		model.addAttribute("authorized_group", "0");
		String authorizationTokenInSession = (String) request.getSession().getAttribute("authorization_token");
		log.debug("Authorization header={}, authorization token in session={}", authorizationHeader, authorizationTokenInSession);
		String uuidString = authorizationHeader != null ? authorizationHeader : authorizationTokenInSession;
		if (uuidString == null) {
			response.setStatus(401);
			log.info("Missing authentication");
			return;
		}
		UUID authorizationUUID = null;
		try {
			authorizationUUID = UUID.fromString(uuidString);
		} catch (IllegalArgumentException e) {
			response.setStatus(400);
			log.info("Malformed authorization UUID={}", uuidString);
			return;
		}
		Long groupTelegramId = telegramController.getGroupIdByAuthorizedUserUUID(authorizationUUID);
		if (groupTelegramId == null) {
			response.setStatus(403);
			log.info("Unrecognized user. Authorization={}", authorizationUUID);
			return;
		}
		model.addAttribute("authorized_group", groupTelegramId);
	}

	@GetMapping("/{groupId}/messages")
	public List<Message> getAllMessages(@PathVariable String groupId, @ModelAttribute("authorized_group") String authorizedGroup, HttpServletResponse response) {
		if (groupId.equals(authorizedGroup) || test) {
			return messageService.getAllByGroupTelegramId(groupId);
		} else {
			log.info("Authorization doesn't match queried group. Queried group={}, authorized group={}", groupId, authorizedGroup);
			response.setStatus(403);
			return Collections.emptyList();
		}
	}

	@GetMapping("/{groupId}/join-events")
	public List<JoinEvent> getAllJoinEvents(@PathVariable String groupId, @ModelAttribute("authorized_group") String authorizedGroup, HttpServletResponse response) {
		if (groupId.equals(authorizedGroup) || test) {
			return joinEventService.getAllByGroupTelegramId(groupId);
		} else {
			log.info("Authorization doesn't match queried group. Queried group={}, authorized group={}", groupId, authorizedGroup);
			response.setStatus(403);
			return Collections.emptyList();
		}
	}

	@GetMapping("/{groupId}/leave-events")
	public List<LeaveEvent> getAllLeaveEvents(@PathVariable String groupId, @ModelAttribute("authorized_group") String authorizedGroup, HttpServletResponse response) {
		if (groupId.equals(authorizedGroup) || test) {
			return leaveEventService.getAllByGroupTelegramId(groupId);
		} else {
			log.info("Authorization doesn't match queried group. Queried group={}, authorized group={}", groupId, authorizedGroup);
			response.setStatus(403);
			return Collections.emptyList();
		}
	}

	@GetMapping("/{groupId}/users")
	public List<User> getAllUsers(@PathVariable String groupId, @ModelAttribute("authorized_group") String authorizedGroup, HttpServletResponse response) {
		if (groupId.equals(authorizedGroup) || test) {
			return userInGroupService.getUsersByGroupTelegramId(groupId);
		} else {
			log.info("Authorization doesn't match queried group. Queried group={}, authorized group={}", groupId, authorizedGroup);
			response.setStatus(403);
			return Collections.emptyList();
		}
	}
}
