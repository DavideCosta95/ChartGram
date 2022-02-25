package chartgram.telegram;

import chartgram.config.Configuration;
import chartgram.config.Language;
import chartgram.config.Localization;
import chartgram.persistence.entity.*;
import chartgram.persistence.service.*;
import chartgram.telegram.model.Command;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TelegramController {
	private final ITelegramBot bot;
	private final Language language;
	private final UserService userService;
	private final MessageService messageService;
	private final GroupService groupService;
	private final JoinEventService joinEventService;
	private final LeaveEventService leaveEventService;
	private final Configuration configuration;

	private Map<String, User> knownUsers;
	private Map<String, Group> knownGroups;
	private final Map<UUID, Long> groupAccessAuthorizations;

	@Autowired
	private TelegramController(Configuration configuration, ITelegramBot bot, Localization localization, UserService userService,
							   MessageService messageService, GroupService groupService, JoinEventService joinEventService, LeaveEventService leaveEventService) {
		this.knownUsers = new HashMap<>();
		this.knownGroups = new HashMap<>();
		this.groupAccessAuthorizations = new HashMap<>();
		this.bot = bot;
		this.configuration = configuration;
		String languageName = configuration.getLanguage();
		this.language = localization.getLanguage(languageName);
		this.userService = userService;
		this.messageService = messageService;
		this.groupService = groupService;
		this.joinEventService = joinEventService;
		this.leaveEventService = leaveEventService;
	}

	public void startup() {
		bot.addOnGroupMessageReceivedHandler(this::handleGroupMessage);
		bot.addOnPrivateMessageReceivedHandler(this::handlePrivateMessage);
		bot.addOnJoiningUserHandler(this::handleJoinUpdate);
		bot.addOnLeavingUserHandler(this::handleLeaveUpdate);
		knownUsers = userService.getAll().stream().collect(Collectors.toMap(User::getTelegramId, Function.identity()));
		knownGroups = groupService.getAll().stream().collect(Collectors.toMap(Group::getTelegramId, Function.identity()));
	}

	private void handleGroupMessage(Update update) {
		org.telegram.telegrambots.meta.api.objects.User sender = update.getMessage().getFrom();

		if (Boolean.TRUE.equals(sender.getIsBot())) {
			return;
		}

		LocalDateTime now = LocalDateTime.now();
		org.telegram.telegrambots.meta.api.objects.Message incomingMessage = update.getMessage();
		String text = incomingMessage.getText();

		User user = new User(sender.getId().toString(), sender.getFirstName(), sender.getLastName(), sender.getUserName(), now);
		user = addKnownUser(user);

		Group group = new Group(update.getMessage().getChatId().toString(), update.getMessage().getChat().getDescription(), now);
		group = addKnownGroup(group);

		int messageTypeId = getMessageType(incomingMessage).getId();
		Message message = new Message(now, user, group, text, messageTypeId);
		messageService.add(message);

		if (incomingMessage.isCommand()) {
			Command command = getCommandByString(text);
			handleGroupCommand(update, command);
		}
	}

	private Command getCommandByString(String text) {
		// TODO: migliorare con factory
		if (text.contains("/analytics")) {
			return Command.ANALYTICS;
		}
		return Command.UNKNOWN;
	}

	private void handleGroupCommand(Update update, Command command) {
		Long senderId = update.getMessage().getFrom().getId();
		Long groupId = update.getMessage().getChatId();
		boolean isGroupAdmin = bot.getAGroupAdmins(groupId).contains(senderId);

		if (isGroupAdmin) {
			switch (command) {
				case ANALYTICS:
					UUID uuid = UUID.randomUUID();
					groupAccessAuthorizations.put(uuid, groupId);
					String webappBaseUrl = configuration.getWebappConfiguration().getBaseUrl();
					int webappPort = configuration.getWebappConfiguration().getPort();

					// TODO: parametrizzare
					String textToSend = webappBaseUrl + ":" + webappPort + "/webapp/groups/" + groupId + "/?authorization=" + uuid;
					log.debug("Generated url={}", textToSend);
					bot.sendMessageToSingleChat(textToSend, senderId.toString());
					bot.sendMessageToSingleChat(language.getLinkSentInPvtText(), groupId.toString());
					break;
				case UNKNOWN:
				default:
					log.debug("Unrecognized command={}", update.getMessage().getText());
					bot.sendMessageToSingleChat(language.getUnknownCommandText(), groupId.toString());
					break;
			}
		} else {
			bot.sendMessageToSingleChat(language.getMustBeAdminText(), groupId.toString());
		}
	}

	private void handlePrivateMessage(Update update) {
		org.telegram.telegrambots.meta.api.objects.Message message = update.getMessage();

		if (message.hasText()) {
			handleTextUpdate(update);
		} else {
			handleNonTextUpdate(update);
		}
	}

	private void handleNonTextUpdate(Update update) {
		Chat chat = update.getMessage().getChat();
		boolean ignoreNonCommandMessages = configuration.getBotConfiguration().getIgnoreNonCommandsMessages();

		if (!ignoreNonCommandMessages) {
			bot.sendMessageToSingleChat(language.getNonCommandText(), chat.getId().toString());
		}
	}

	private void handleTextUpdate(Update update) {
		org.telegram.telegrambots.meta.api.objects.User sender = update.getMessage().getFrom();
		Chat chat = update.getMessage().getChat();
		String receivedText = update.getMessage().getText();
		log.info("Sender={} - received text={}", sender, receivedText);

		boolean ignoreNonCommandMessages = configuration.getBotConfiguration().getIgnoreNonCommandsMessages();

		if (receivedText.startsWith("/")) {
			handlePrivateCommand(update);
		} else if (Boolean.FALSE.equals(ignoreNonCommandMessages)) {
			bot.sendMessageToSingleChat(language.getNonCommandText(), chat.getId().toString());
		}
	}

	private void handlePrivateCommand(Update update) {
		Command command = getCommandByString(update.getMessage().getText());
		String senderId = update.getMessage().getFrom().getId().toString();
		switch (command) {
			case ANALYTICS:
				bot.sendMessageToSingleChat(language.getPrivateCommandNotAllowedText(), senderId);
				break;
			case UNKNOWN:
			default:
				bot.sendMessageToSingleChat(language.getUnknownCommandText(), senderId);
				break;
		}
	}

	public Long getGroupIdByAuthorizedUserUUID(UUID uuid) {
		return groupAccessAuthorizations.get(uuid);
	}

	private MessageType getMessageType(org.telegram.telegrambots.meta.api.objects.Message message) {
		boolean hasMedia =
				message.hasVoice()
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

		if (!hasMedia) {
			return MessageType.TEXT;
		}
		if (message.hasVoice()) {
			return MessageType.AUDIO;
		}
		if (message.hasPhoto()) {
			return MessageType.PHOTO;
		}
		if (message.hasSticker()) {
			return MessageType.STICKER;
		}
		if (message.hasVideo()) {
			return MessageType.VIDEO;
		}
		if (message.hasAnimation()) {
			return MessageType.GIF;
		}
		return MessageType.OTHER;
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

	// TODO: aggiungere controllo e aggiunta a users_in_groups
	private User addKnownUser(@NonNull User user) {
		User persistedUser = knownUsers.get(user.getTelegramId());
		if (persistedUser == null) {
			log.info("Found new user={}", user);
			persistedUser = userService.add(user);
			knownUsers.put(persistedUser.getTelegramId(), persistedUser);
		} else {
			boolean modified = false;
			if (!Objects.equals(persistedUser.getTelegramFirstName(), user.getTelegramFirstName())) {
				persistedUser.setTelegramFirstName(user.getTelegramFirstName());
				modified = true;
			}
			if (!Objects.equals(persistedUser.getTelegramLastName(), user.getTelegramLastName())) {
				persistedUser.setTelegramLastName(user.getTelegramLastName());
				modified = true;
			}
			if (!Objects.equals(persistedUser.getTelegramUsername(), user.getTelegramUsername())) {
				persistedUser.setTelegramUsername(user.getTelegramUsername());
				modified = true;
			}
			if (modified) {
				persistedUser = userService.add(persistedUser);
			}
		}
		return persistedUser;
	}

	private Group addKnownGroup(@NonNull Group group) {
		Group persistedGroup = knownGroups.get(group.getTelegramId());
		if (persistedGroup == null) {
			persistedGroup = groupService.add(group);
			knownGroups.put(group.getTelegramId(), group);
		} else {
			if (!Objects.equals(persistedGroup.getDescription(), group.getDescription())) {
				persistedGroup.setDescription(group.getDescription());
				persistedGroup = groupService.add(persistedGroup);
			}
		}
		return persistedGroup;
	}
}
