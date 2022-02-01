package chartgram;

import chartgram.persistence.entity.Group;
import chartgram.persistence.entity.Message;
import chartgram.persistence.entity.User;
import chartgram.persistence.service.GroupService;
import chartgram.persistence.service.MessageService;
import chartgram.persistence.service.UserService;
import chartgram.telegram.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MessageController {
	private final TelegramBot bot;
	private final UserService userService;
	private final MessageService messageService;
	private final GroupService groupService;

	@Autowired
	private MessageController(TelegramBot bot, UserService userService, MessageService messageService, GroupService groupService) {
		this.bot = bot;
		this.userService = userService;
		this.messageService = messageService;
		this.groupService = groupService;
	}

	public void startup() {
		bot.addOnGroupMessageReceivedHandler(this::handleGroupMessage);
	}

	private void handleGroupMessage(Update update) {
		LocalDateTime now = LocalDateTime.now();
		org.telegram.telegrambots.meta.api.objects.User sender = update.getMessage().getFrom();
		String text = update.getMessage().getText();

		User user = new User(sender.getId().toString(), sender.getFirstName(), sender.getLastName(), sender.getUserName(), now);
		Group group = new Group(update.getMessage().getChatId().toString(), update.getMessage().getChat().getDescription(), now);

		user = userService.add(user);
		group = groupService.add(group);

		// TODO: migliorare riconoscimento media presente
		Message message = new Message(now, user, group, text, text == null);

		messageService.add(message);
	}
}
