package chartgram.api;

import chartgram.config.Configuration;
import chartgram.model.Pair;
import chartgram.persistence.entity.*;
import chartgram.persistence.service.*;
import chartgram.persistence.utils.TemporalEventComparator;
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
import org.springframework.http.HttpHeaders;

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
		Pair<User, Group> authorizationData = telegramController.getAuthorizationDataByUserUUID(authorizationUUID);
		if (authorizationData == null) {
			response.setStatus(403);
			log.info("Unrecognized user. Authorization={}", authorizationUUID);
			return;
		}
		model.addAttribute("authorized_group", authorizationData.getSecond().getTelegramId());
	}

	// TODO: introdurre ApiResponseBean senza dati ridondanti
	@GetMapping("/{groupId}/messages")
	public List<Message> getAllMessages(@PathVariable String groupId, @ModelAttribute("authorized_group") String authorizedGroup, HttpServletResponse response) {
		if (groupId.equals(authorizedGroup) || test) {
			response.setStatus(200);
			response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
			List<Message> messages = messageService.getAllByGroupTelegramId(groupId);
			messages.sort(new TemporalEventComparator());
			return messages;
		} else {
			log.info("Authorization doesn't match queried group. Queried group={}, authorized group={}", groupId, authorizedGroup);
			response.setStatus(403);
			return Collections.emptyList();
		}
	}

	@GetMapping("/{groupId}/join-events")
	public List<JoinEvent> getAllJoinEvents(@PathVariable String groupId, @ModelAttribute("authorized_group") String authorizedGroup, HttpServletResponse response) {
		if (groupId.equals(authorizedGroup) || test) {
			response.setStatus(200);
			response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
			List<JoinEvent> joinEvents = joinEventService.getAllByGroupTelegramId(groupId);
			joinEvents.sort(new TemporalEventComparator());
			return joinEvents;
		} else {
			log.info("Authorization doesn't match queried group. Queried group={}, authorized group={}", groupId, authorizedGroup);
			response.setStatus(403);
			return Collections.emptyList();
		}
	}

	@GetMapping("/{groupId}/leave-events")
	public List<LeaveEvent> getAllLeaveEvents(@PathVariable String groupId, @ModelAttribute("authorized_group") String authorizedGroup, HttpServletResponse response) {
		if (groupId.equals(authorizedGroup) || test) {
			response.setStatus(200);
			response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
			List<LeaveEvent> leaveEvents = leaveEventService.getAllByGroupTelegramId(groupId);
			leaveEvents.sort(new TemporalEventComparator());
			return leaveEvents;
		} else {
			log.info("Authorization doesn't match queried group. Queried group={}, authorized group={}", groupId, authorizedGroup);
			response.setStatus(403);
			return Collections.emptyList();
		}
	}

	@GetMapping("/{groupId}/users")
	public List<User> getAllUsers(@PathVariable String groupId, @ModelAttribute("authorized_group") String authorizedGroup, HttpServletResponse response) {
		if (groupId.equals(authorizedGroup) || test) {
			response.setStatus(200);
			response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
			return userInGroupService.getUsersByGroupTelegramId(groupId);
		} else {
			log.info("Authorization doesn't match queried group. Queried group={}, authorized group={}", groupId, authorizedGroup);
			response.setStatus(403);
			return Collections.emptyList();
		}
	}
}
