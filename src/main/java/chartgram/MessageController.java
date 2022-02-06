package chartgram;

import chartgram.persistence.entity.*;
import chartgram.persistence.service.*;
import chartgram.telegram.TelegramBot;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Component
@Slf4j
public class MessageController {
	private final TelegramBot bot;
	private final UserService userService;
	private final MessageService messageService;
	private final GroupService groupService;
	private final JoinEventService joinEventService;
	private final LeaveEventService leaveEventService;

	private final Map<String, User> knownUsers;

	@Autowired
	private MessageController(TelegramBot bot, UserService userService, MessageService messageService, GroupService groupService, JoinEventService joinEventService, LeaveEventService leaveEventService) {
		this.knownUsers = new HashMap<>();
		this.bot = bot;
		this.userService = userService;
		this.messageService = messageService;
		this.groupService = groupService;
		this.joinEventService = joinEventService;
		this.leaveEventService = leaveEventService;
	}

	public void startup() {
		bot.addOnGroupMessageReceivedHandler(this::handleGroupMessage);
		bot.addOnJoiningUserHandler(this::handleJoinUpdate);
		bot.addOnLeavingUserHandler(this::handleLeaveUpdate);
	}

	private void handleGroupMessage(Update update) {
		LocalDateTime now = LocalDateTime.now();
		org.telegram.telegrambots.meta.api.objects.Message incomingMessage = update.getMessage();
		String text = incomingMessage.getText();
		org.telegram.telegrambots.meta.api.objects.User sender = incomingMessage.getFrom();

		User user = new User(sender.getId().toString(), sender.getFirstName(), sender.getLastName(), sender.getUserName(), now);
		Group group = new Group(update.getMessage().getChatId().toString(), update.getMessage().getChat().getDescription(), now);

		user = userService.add(user);
		group = groupService.add(group);

		Message message = new Message(now, user, group, text, hasMedia(incomingMessage));

		addKnownUser(user);
		messageService.add(message);
	}

	private boolean hasMedia(org.telegram.telegrambots.meta.api.objects.Message message) {
		return message.hasVoice()
				|| message.hasAnimation()
				|| message.hasAudio()
				|| message.hasContact()
				|| message.hasDice()
				|| message.hasDocument()
				|| message.hasLocation()
				|| message.hasPhoto()
				|| message.hasSticker()
				|| message.hasInvoice()
				|| message.hasSuccessfulPayment()
				|| message.hasPassportData()
				|| message.hasVideo()
				|| message.hasVideoNote();
	}

	private void handleJoinUpdate(Update update) {
		org.telegram.telegrambots.meta.api.objects.User sender = update.getMessage().getFrom();
		LocalDateTime now = LocalDateTime.now();

		List<JoinEvent> joinEvents = new ArrayList<>(update.getMessage().getNewChatMembers().size());

		update.getMessage()
				.getNewChatMembers()
				.stream()
				.filter(Predicate.not(org.telegram.telegrambots.meta.api.objects.User::getIsBot))
				.map(u -> new User(u.getId().toString(), u.getFirstName(), u.getLastName(), u.getUserName(), now))
				.forEach(u -> {
					JoinEvent currentJoinEvent = new JoinEvent();
					currentJoinEvent.setJoinedAt(now);
					currentJoinEvent.setJoiningUser(u);
					if (!u.getId().equals(sender.getId())) {
						User adder = new User(sender.getId().toString(), sender.getFirstName(), sender.getLastName(), sender.getUserName(), now);
						currentJoinEvent.setAdderUser(adder);
					}
					joinEvents.add(currentJoinEvent);
				});
		joinEventService.addAll(joinEvents);
	}

	private void handleLeaveUpdate(Update update) {
		org.telegram.telegrambots.meta.api.objects.User sender = update.getMessage().getFrom();
		LocalDateTime now = LocalDateTime.now();

		org.telegram.telegrambots.meta.api.objects.User leavingUser = update.getMessage().getLeftChatMember();
		if (!leavingUser.getIsBot()) {
			User user = new User(leavingUser.getId().toString(), leavingUser.getFirstName(), leavingUser.getLastName(), leavingUser.getUserName(), now);
			LeaveEvent leaveEvent = new LeaveEvent();
			leaveEvent.setLeavingAt(now);
			leaveEvent.setLeavingUser(user);

			if (!sender.getId().equals(user.getId())) {
				User removerUser = new User(sender.getId().toString(), sender.getFirstName(), sender.getLastName(), sender.getUserName(), now);
				leaveEvent.setRemoverUser(removerUser);
			}
			leaveEventService.add(leaveEvent);
		}
	}

	private void addKnownUser(@NonNull User user) {
		User oldValue = this.knownUsers.put(user.getTelegramId(), user);
		if (!user.equals(oldValue)) {
			userService.add(user);
		}
	}

	private void cleanKnownUsers() {
		this.knownUsers.clear();
	}

	private void removeKnownUser(String telegramId) {
		this.knownUsers.remove(telegramId);
	}
}
