package chartgram.webapp;

import chartgram.config.Configuration;
import chartgram.exceptions.ApiCommunicationException;
import chartgram.model.Pair;
import chartgram.persistence.entity.Group;
import chartgram.persistence.entity.User;
import chartgram.persistence.service.ServicesWrapper;
import chartgram.telegram.TelegramController;
import chartgram.telegram.model.ITelegramBot;
import chartgram.webclient.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Collections;
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
	private final HttpClient httpClient;
	private final boolean test;

	@Autowired
	private HomeController(Configuration configuration, TelegramController telegramController, ITelegramBot telegramBot, ServicesWrapper servicesWrapper, HttpClient httpClient) {
		this.configuration = configuration;
		this.telegramBot = telegramBot;
		this.telegramController = telegramController;
		this.servicesWrapper = servicesWrapper;
		this.httpClient = httpClient;
		this.test = configuration.isTest();
	}

	@GetMapping("/{groupId}")
	public String home(@PathVariable("groupId") String groupId, @RequestParam("authorization") Optional<String> authorization, Model model, HttpServletRequest request, HttpServletResponse response) {
		UUID authorizationUUID;
		Pair<User, Group> authorizationData;
		User user;
		Group group;

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

			user = authorizationData.getFirst();
			group = authorizationData.getSecond();

			request.getSession().setAttribute("authorization_token", authorizationUUID.toString());
			model.addAttribute("authorization_token", authorizationUUID.toString());
			model.addAttribute("user_propic", Base64.getEncoder().encodeToString(getUserPropicFile(user.getTelegramId())));
		}
		else {
			Pair<User, UUID> testAuthorization = getTestModeAuthorizationData(authorization);
			user = testAuthorization.getFirst();
			UUID uuid = testAuthorization.getSecond();
			if (uuid == null) {
				model.addAttribute("user_propic", Base64.getEncoder().encodeToString(getDefaultUserPropicAsByteArray()));
			} else {
				request.getSession().setAttribute("authorization_token", uuid.toString());
				model.addAttribute("authorization_token", uuid.toString());
				model.addAttribute("user_propic", Base64.getEncoder().encodeToString(getUserPropicFile(user.getTelegramId())));
			}
			group = new Group(groupId, servicesWrapper.getGroupService().getByTelegramId(groupId).getTitle());
		}

		model.addAttribute("group", group);
		model.addAttribute("user", user);
		model.addAttribute("members_count", telegramBot.getGroupMembersCount(groupId));
		model.addAttribute("admins_count", telegramBot.getAGroupAdmins(groupId).size());
		model.addAttribute("messages_count", servicesWrapper.getMessageService().getCountByGroupTelegramId(groupId));
		model.addAttribute("api_url", configuration.getWebappConfiguration().getBaseUrl() + ":" + configuration.getWebappConfiguration().getPort() + "/api");
		return "index";
	}

	private Pair<User, UUID> getTestModeAuthorizationData(Optional<String> authorizationString) {
		Pair<User, UUID> authorization = new Pair<>();
		User testUser = new User("1", "Test User", "", "");
		authorization.setFirst(testUser);
		authorization.setSecond(null);

		if (authorizationString.isEmpty()) {
			return authorization;
		}

		UUID authorizationUUID;
		try {
			authorizationUUID = UUID.fromString(authorizationString.get());
		} catch (IllegalArgumentException e) {
			return authorization;
		}

		Pair<User, Group> authorizationData = telegramController.getAuthorizationDataByUserUUID(authorizationUUID);
		if (authorizationData == null) {
			return authorization;
		}
		authorization.setFirst(authorizationData.getFirst());
		authorization.setSecond(authorizationUUID);
		return authorization;
	}

	private byte[] getUserPropicFile(String userId) {
		var userPropic = telegramBot.getLatestUserPropic(Long.parseLong(userId));
		if (userPropic == null) {
			return new byte[0];
		}
		String filePath = telegramBot.getFileFromApi(userPropic.getFileId());
		try {
			return httpClient.doHttpGet("https://api.telegram.org/file/bot" + configuration.getBotConfiguration().getToken() + "/" + filePath, Collections.emptyMap());
		} catch (ApiCommunicationException e) {
			log.error("Error getting latest profile picture of user with Telegram id={}", userId, e);
			return new byte[0];
		}
	}

	private byte[] getDefaultUserPropicAsByteArray() {
		URL imageUrl = getClass().getResource("/static/img/default_user_profile_picture.jpg");
		if (imageUrl == null) {
			return new byte[0];
		}

		try {
			BufferedImage bImage = ImageIO.read(imageUrl);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(bImage, "jpg", bos );
			return bos.toByteArray();
		} catch (IOException e) {
			log.error("Cannot read default user profile picture", e);
			return new byte[0];
		}
	}
}
