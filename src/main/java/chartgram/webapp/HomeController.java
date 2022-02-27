package chartgram.webapp;

import chartgram.config.Configuration;
import chartgram.telegram.TelegramController;
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
	private static final String MODEL_ATTRIBUTE_ERROR_NAME = "error_type";
	private final TelegramController telegramController;
	private final boolean test;

	@Autowired
	private HomeController(Configuration configuration, TelegramController telegramController) {
		this.telegramController = telegramController;
		this.test = configuration.isTest();
	}

	@GetMapping("/{groupId}")
	public String home(@PathVariable("groupId") String groupId, @RequestParam("authorization") Optional<String> authorization, Model model, HttpServletRequest request, HttpServletResponse response) {
		UUID authorizationUUID;
		if (!test) {
			if (authorization.isEmpty()) {
				response.setStatus(401);
				model.addAttribute(MODEL_ATTRIBUTE_ERROR_NAME, "Missing authorization token");
				return ERROR_PAGE;
			}

			try {
				authorizationUUID = UUID.fromString(authorization.get());
			} catch (IllegalArgumentException e) {
				response.setStatus(400);
				log.info("Malformed authorization UUID={}", authorization.get());
				model.addAttribute(MODEL_ATTRIBUTE_ERROR_NAME, "Malformed authorization token");
				return ERROR_PAGE;
			}

			Long groupTelegramId = telegramController.getGroupIdByAuthorizedUserUUID(authorizationUUID);
			if (groupTelegramId == null) {
				response.setStatus(403);
				log.info("Unrecognized user. Authorization={}", authorizationUUID);
				model.addAttribute(MODEL_ATTRIBUTE_ERROR_NAME, "Unrecognized user");
				return ERROR_PAGE;
			}
		}
		else {
			authorizationUUID = UUID.randomUUID();
		}
		request.getSession().setAttribute("authorization_token", authorizationUUID.toString());
		model.addAttribute("authorization_token", authorizationUUID.toString());
		model.addAttribute("group_id", groupId);
		return "charts";
	}
}
