package chartgram.webapp;

import chartgram.config.Configuration;
import chartgram.model.Pair;
import chartgram.persistence.entity.Group;
import chartgram.persistence.entity.User;
import chartgram.persistence.service.ServicesWrapper;
import chartgram.telegram.TelegramController;
import chartgram.telegram.model.ITelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@Controller
@Slf4j
@RequestMapping("/webapp/groups")
public class HomeController {
	private static final String ERROR_PAGE = "error";
	private static final String MODEL_ATTRIBUTE_ERROR_MESSAGE = "error_message";
	private static final String MODEL_ATTRIBUTE_ERROR_CODE = "error_code";
	private final Configuration configuration;
	private final TelegramController telegramController;
	private final ITelegramBot telegramBot;
	private final ServicesWrapper servicesWrapper;
	private final boolean test;

	@Autowired
	private HomeController(Configuration configuration, TelegramController telegramController, ITelegramBot telegramBot, ServicesWrapper servicesWrapper) {
		this.configuration = configuration;
		this.telegramBot = telegramBot;
		this.telegramController = telegramController;
		this.servicesWrapper = servicesWrapper;
		this.test = configuration.isTest();
	}

	@GetMapping("/{groupId}")
	public String home(@PathVariable("groupId") String groupId, @RequestParam("authorization") Optional<String> authorization, Model model, HttpServletRequest request, HttpServletResponse response) {
		UUID authorizationUUID;
		Pair<User, Group> authorizationData;
		if (!test) {
			if (authorization.isEmpty()) {
				response.setStatus(401);
				model.addAttribute(MODEL_ATTRIBUTE_ERROR_CODE, "401");
				model.addAttribute(MODEL_ATTRIBUTE_ERROR_MESSAGE, "Missing authorization token");
				return ERROR_PAGE;
			}

			try {
				authorizationUUID = UUID.fromString(authorization.get());
			} catch (IllegalArgumentException e) {
				response.setStatus(400);
				log.info("Malformed authorization UUID={}", authorization.get());
				model.addAttribute(MODEL_ATTRIBUTE_ERROR_CODE, "400");
				model.addAttribute(MODEL_ATTRIBUTE_ERROR_MESSAGE, "Malformed authorization token");
				return ERROR_PAGE;
			}

			authorizationData = telegramController.getAuthorizationDataByUserUUID(authorizationUUID);
			log.debug("Authorization data={}", authorizationData);

			if (authorizationData == null || !authorizationData.getSecond().getTelegramId().equals(groupId)) {
				response.setStatus(403);
				log.info("Unrecognized user. Authorization={}", authorizationUUID);
				model.addAttribute(MODEL_ATTRIBUTE_ERROR_CODE, "403");
				model.addAttribute(MODEL_ATTRIBUTE_ERROR_MESSAGE, "Unrecognized user");
				return ERROR_PAGE;
			}
		}
		else {
			authorizationUUID = UUID.randomUUID();
			authorizationData = new Pair<>(new User("0", "Test User", "", ""), new Group("1", "Test_Group"));
		}

		User user = authorizationData.getFirst();
		Group group = authorizationData.getSecond();

		request.getSession().setAttribute("authorization_token", authorizationUUID.toString());
		model.addAttribute("authorization_token", authorizationUUID.toString());
		model.addAttribute("group", group);
		model.addAttribute("user", user);
		model.addAttribute("members_count", telegramBot.getGroupMembersCount(group.getTelegramId()));
		model.addAttribute("admins_count", telegramBot.getAGroupAdmins(group.getTelegramId()).size());
		model.addAttribute("messages_count", servicesWrapper.getMessageService().getCountByGroupTelegramId(group.getTelegramId()));
		model.addAttribute("api_url", configuration.getWebappConfiguration().getBaseUrl() + ":" + configuration.getWebappConfiguration().getPort() + "/api");
		return "index";
	}
}
