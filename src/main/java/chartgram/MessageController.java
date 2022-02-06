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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MessageController {
	private final TelegramBot bot;
	private final UserService userService;
	private final MessageService messageService;
	private final GroupService groupService;
	private final JoinEventService joinEventService;
	private final LeaveEventService leaveEventService;

	private Map<String, User> knownUsers;
	private Map<String, Group> knownGroups;

	@Autowired
	private MessageController(TelegramBot bot, UserService userService, MessageService messageService, GroupService groupService, JoinEventService joinEventService, LeaveEventService leaveEventService) {
		this.knownUsers = new HashMap<>();
		this.knownGroups = new HashMap<>();
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
		knownUsers = userService.list().stream().collect(Collectors.toMap(User::getTelegramId, Function.identity()));
		knownGroups = groupService.list().stream().collect(Collectors.toMap(Group::getTelegramId, Function.identity()));
	}

	private void handleGroupMessage(Update update) {
		LocalDateTime now = LocalDateTime.now();
		org.telegram.telegrambots.meta.api.objects.Message incomingMessage = update.getMessage();
		String text = incomingMessage.getText();
		org.telegram.telegrambots.meta.api.objects.User sender = incomingMessage.getFrom();

		User user = new User(sender.getId().toString(), sender.getFirstName(), sender.getLastName(), sender.getUserName(), now);
		user = addKnownUser(user);

		Group group = new Group(update.getMessage().getChatId().toString(), update.getMessage().getChat().getDescription(), now);
		group = addKnownGroup(group);

		Message message = new Message(now, user, group, text, hasMedia(incomingMessage));
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

	private User addKnownUser(@NonNull User user) {
		User persistedUser = knownUsers.get(user.getTelegramId());
		if (persistedUser == null) {
			persistedUser = userService.add(user);
			knownUsers.put(user.getTelegramId(), user);
		}
		return persistedUser;
	}

	private Group addKnownGroup(@NonNull Group group) {
		Group persistedGroup = knownGroups.get(group.getTelegramId());
		if (persistedGroup == null) {
			persistedGroup = groupService.add(group);
			knownGroups.put(group.getTelegramId(), group);
		}
		return persistedGroup;
	}
}
