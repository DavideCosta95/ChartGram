package chartgram.telegram;

import chartgram.charts.ChartController;
import chartgram.charts.model.Chart;
import chartgram.charts.model.ChartType;
import chartgram.config.Configuration;
import chartgram.config.Locale;
import chartgram.config.Localization;
import chartgram.model.Pair;
import chartgram.persistence.entity.*;
import chartgram.persistence.service.*;
import chartgram.telegram.model.Command;
import chartgram.telegram.model.ITelegramBot;
import chartgram.telegram.model.MessageType;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Transactional
public class TelegramController {
	private final ITelegramBot bot;
	private final Locale locale;
	private final ServicesWrapper servicesWrapper;
	private final Configuration configuration;
	private final ChartController chartController;

	private Map<String, User> knownUsers;
	private Map<String, Group> knownGroups;
	private final Map<String, List<String>> userTelegramId2GroupMemberships;
	private final Map<UUID, Pair<User, Group>> groupAccessAuthorizations;

	public TelegramController(Configuration configuration, ChartController chartController, ITelegramBot bot, Localization localization, ServicesWrapper servicesWrapper) {
		this.knownUsers = new HashMap<>();
		this.knownGroups = new HashMap<>();
		this.userTelegramId2GroupMemberships = new HashMap<>();
		this.groupAccessAuthorizations = new HashMap<>();
		this.configuration = configuration;
		this.chartController = chartController;
		this.bot = bot;
		String languageName = configuration.getLanguage();
		this.locale = localization.getLocaleByLanguage(languageName);
		this.servicesWrapper = servicesWrapper;
	}

	public void startup() {
		bot.addOnGroupMessageReceivedHandler(this::handleGroupMessage);
		bot.addOnPrivateMessageReceivedHandler(this::handlePrivateMessage);
		bot.addOnJoiningUserHandler(this::handleJoinUpdate);
		bot.addOnLeavingUserHandler(this::handleLeaveUpdate);
		knownUsers = servicesWrapper.getUserService().getAll().stream().collect(Collectors.toMap(User::getTelegramId, Function.identity()));
		knownGroups = servicesWrapper.getGroupService().getAll().stream().collect(Collectors.toMap(Group::getTelegramId, Function.identity()));
		servicesWrapper.getUserInGroupService().getAll().forEach(e -> {
			List<String> currentMemberships = userTelegramId2GroupMemberships.computeIfAbsent(e.getUser().getTelegramId(), k -> new ArrayList<>());
			currentMemberships.add(e.getGroup().getTelegramId());
		});
	}

	private void handleGroupMessage(Update update) {
		org.telegram.telegrambots.meta.api.objects.User sender = update.getMessage().getFrom();
		String groupId = update.getMessage().getChatId().toString();

		if (Boolean.TRUE.equals(sender.getIsBot())) {
			return;
		}

		LocalDateTime now = LocalDateTime.now();
		org.telegram.telegrambots.meta.api.objects.Message incomingMessage = update.getMessage();
		String text = incomingMessage.getText();

		Group group = new Group(groupId, update.getMessage().getChat().getTitle(), now);
		group = addKnownGroup(group);

		User user = new User(sender.getId().toString(), sender.getFirstName(), sender.getLastName(), sender.getUserName(), now);
		user = addKnownUser(user, group);

		int messageTypeId = getMessageType(incomingMessage).getId();
		Message message = new Message(now, user, group, text, messageTypeId);
		servicesWrapper.getMessageService().add(message);

		if (incomingMessage.isCommand()) {
			Command command = getCommandByString(text);
			handleGroupCommand(update, command);
		}
	}

	private Command getCommandByString(String text) {
		// TODO: migliorare con factory
		if (text.contains(configuration.getBotConfiguration().getStartCommand())) {
			return Command.START;
		}
		if (text.contains(configuration.getBotConfiguration().getHelpCommand())) {
			return Command.HELP;
		}
		if (text.contains(configuration.getBotConfiguration().getAnalyticsCommand())) {
			return Command.ANALYTICS;
		}
		if (text.contains(configuration.getBotConfiguration().getChartsCommand())) {
			return Command.CHARTS;
		}
		return Command.UNKNOWN;
	}

	private void handleGroupCommand(Update update, Command command) {
		org.telegram.telegrambots.meta.api.objects.User sender = update.getMessage().getFrom();
		Long senderId = sender.getId();
		Chat groupChat = update.getMessage().getChat();
		Long groupId = update.getMessage().getChatId();
		String groupTitle = groupChat.getTitle();
		boolean isGroupAdmin = bot.getAGroupAdmins(groupId.toString()).contains(senderId);

		if (isGroupAdmin) {
			switch (command) {
				case START:
					bot.sendMessageToSingleChat(locale.getGenericSentViaPvtText(), groupId.toString());
					bot.sendMessageToSingleChat(locale.getStartCommandText(), senderId.toString());
					break;
				case HELP:
					bot.sendMessageToSingleChat(locale.getGenericSentViaPvtText(), groupId.toString());
					bot.sendMessageToSingleChat(locale.getHelpCommandText(), senderId.toString());
					break;
				case ANALYTICS:
					if (!bot.getAGroupAdmins(groupId.toString()).contains(bot.getBotId())) {
						bot.sendMessageToSingleChat(locale.getBotMustBeAdminText(), groupId.toString());
					} else {
						UUID uuid = UUID.randomUUID();
						groupAccessAuthorizations.put(uuid, new Pair<>(new User(senderId.toString(), sender.getFirstName(), sender.getLastName(), sender.getUserName()), new Group(groupId.toString(), groupTitle)));
						String webappBaseUrl = configuration.getWebappConfiguration().getBaseUrl();
						int webappPort = configuration.getWebappConfiguration().getPort();

						// TODO: parametrizzare
						String textToSend = webappBaseUrl + ":" + webappPort + "/webapp/groups/" + groupId + "/?authorization=" + uuid;
						log.debug("Generated url={}", textToSend);
						bot.sendMessageToSingleChat(textToSend, senderId.toString());
						bot.sendMessageToSingleChat(locale.getLinkSentViaPvtText(), groupId.toString());
					}
					break;
				case CHARTS:
					if (!bot.getAGroupAdmins(groupId.toString()).contains(bot.getBotId())) {
						bot.sendMessageToSingleChat(locale.getBotMustBeAdminText(), groupId.toString());
					} else {
						bot.sendMessageToSingleChat(locale.getChartsSentViaPvtText(), groupId.toString());
						sendAllCharts(senderId.toString(), groupId.toString(), groupTitle);
					}
					break;
				case UNKNOWN:
				default:
					log.debug("Unknown command={}", update.getMessage().getText());
					bot.sendMessageToSingleChat(locale.getUnknownCommandText(), groupId.toString());
					break;
			}
		} else {
			bot.sendMessageToSingleChat(locale.getUserMustBeAdminText(), groupId.toString());
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
			bot.sendMessageToSingleChat(locale.getNonCommandText(), chat.getId().toString());
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
			bot.sendMessageToSingleChat(locale.getNonCommandText(), chat.getId().toString());
		}
	}

	private void handlePrivateCommand(Update update) {
		Command command = getCommandByString(update.getMessage().getText());
		String senderId = update.getMessage().getFrom().getId().toString();
		switch (command) {
			case START:
				bot.sendMessageToSingleChat(locale.getStartCommandText(), senderId);
				break;
			case HELP:
				bot.sendMessageToSingleChat(locale.getHelpCommandText(), senderId);
				break;
			case ANALYTICS:
				if (configuration.isTest()) {
					Group firstGroup = servicesWrapper.getGroupService().getFirst();
					String textToSend = configuration.getWebappConfiguration().getBaseUrl() + ":" + configuration.getWebappConfiguration().getPort() + "/webapp/groups/" + firstGroup.getTelegramId();
					log.debug("Generated url={}", textToSend);
					bot.sendMessageToSingleChat(textToSend, senderId);
				} else {
					bot.sendMessageToSingleChat(locale.getPrivateCommandNotAllowedText(), senderId);
				}
				break;
			case CHARTS:
				if (configuration.isTest()) {
					Group firstGroup = servicesWrapper.getGroupService().getFirst();
					if (firstGroup == null) {
						sendAllCharts(senderId, "", "");
					} else {
						sendAllCharts(senderId, firstGroup.getTelegramId(), firstGroup.getTitle());
					}
				} else {
					bot.sendMessageToSingleChat(locale.getPrivateCommandNotAllowedText(), senderId);
				}
				break;
			case UNKNOWN:
			default:
				bot.sendMessageToSingleChat(locale.getUnknownCommandText(), senderId);
				break;
		}
	}

	public Pair<User, Group> getAuthorizationDataByUserUUID(UUID uuid) {
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

	private void sendAllCharts(String recipientId, String groupId, String groupTitle) {
		Chart chart = chartController.getChart(ChartType.MESSAGES_DISTRIBUTION_BY_TYPE, groupId, groupTitle);
		bot.sendImage(chart.getImage(), chart.getCaption(), recipientId);
		chart = chartController.getChart(ChartType.MESSAGES_WITH_RESPECT_TIME, groupId, groupTitle);
		bot.sendImage(chart.getImage(), chart.getCaption(), recipientId);
		chart = chartController.getChart(ChartType.JOINS_DISTRIBUTION_WITH_RESPECT_TIME, groupId, groupTitle);
		bot.sendImage(chart.getImage(), chart.getCaption(), recipientId);
		chart = chartController.getChart(ChartType.LEAVINGS_DISTRIBUTION_WITH_RESPECT_TIME, groupId, groupTitle);
		bot.sendImage(chart.getImage(), chart.getCaption(), recipientId);
		chart = chartController.getChart(ChartType.JOINS_VS_LIVINGS, groupId, groupTitle);
		bot.sendImage(chart.getImage(), chart.getCaption(), recipientId);
	}

	private void handleJoinUpdate(Update update) {
		org.telegram.telegrambots.meta.api.objects.User sender = update.getMessage().getFrom();
		String groupId = update.getMessage().getChatId().toString();
		LocalDateTime now = LocalDateTime.now();

		Group group = new Group(groupId, update.getMessage().getChat().getTitle(), now);
		final Group persistedGroup = addKnownGroup(group);

		List<JoinEvent> joinEvents = new ArrayList<>(update.getMessage().getNewChatMembers().size());

		update.getMessage()
				.getNewChatMembers()
				.stream()
				.filter(Predicate.not(org.telegram.telegrambots.meta.api.objects.User::getIsBot))
				.map(u -> new User(u.getId().toString(), u.getFirstName(), u.getLastName(), u.getUserName(), now))
				.forEach(u -> {
					User persistedJoiningUser = addKnownUser(u, persistedGroup);
					JoinEvent currentJoinEvent = new JoinEvent();
					currentJoinEvent.setJoinedAt(now);
					currentJoinEvent.setJoiningUser(persistedJoiningUser);
					currentJoinEvent.setGroup(persistedGroup);
					if (!persistedJoiningUser.getTelegramId().equals(sender.getId().toString())) {
						User adder = new User(sender.getId().toString(), sender.getFirstName(), sender.getLastName(), sender.getUserName(), now);
						User persistedAdderUser = addKnownUser(adder, persistedGroup);
						currentJoinEvent.setAdderUser(persistedAdderUser);
					}
					joinEvents.add(currentJoinEvent);
				});
		servicesWrapper.getJoinEventService().addAll(joinEvents);
	}

	private void handleLeaveUpdate(Update update) {
		org.telegram.telegrambots.meta.api.objects.User sender = update.getMessage().getFrom();
		String groupId = update.getMessage().getChatId().toString();
		LocalDateTime now = LocalDateTime.now();

		Group group = new Group(groupId, update.getMessage().getChat().getTitle(), now);
		group = addKnownGroup(group);

		org.telegram.telegrambots.meta.api.objects.User leavingUser = update.getMessage().getLeftChatMember();
		if (!leavingUser.getIsBot()) {
			User user = new User(leavingUser.getId().toString(), leavingUser.getFirstName(), leavingUser.getLastName(), leavingUser.getUserName(), now);
			User persistedLeavingUser = addKnownUser(user, group);
			LeaveEvent leaveEvent = new LeaveEvent();
			leaveEvent.setLeavingAt(now);
			leaveEvent.setGroup(group);
			leaveEvent.setLeavingUser(persistedLeavingUser);
			// TODO: rimuovere da users_in_group

			if (!sender.getId().equals(user.getId())) {
				User removerUser = new User(sender.getId().toString(), sender.getFirstName(), sender.getLastName(), sender.getUserName(), now);
				User persistedRemoverUser = addKnownUser(removerUser, group);
				leaveEvent.setRemoverUser(persistedRemoverUser);
			}
			servicesWrapper.getLeaveEventService().add(leaveEvent);
		}
	}

	private User addKnownUser(@NonNull User user, @NonNull Group group) {
		User cachedUser = knownUsers.get(user.getTelegramId());
		if (cachedUser == null) {
			User persistedUser = servicesWrapper.getUserService().getByTelegramId(user.getTelegramId());
			if (persistedUser == null) {
				log.info("Found new user={}", user);
				persistedUser = servicesWrapper.getUserService().add(user);
				servicesWrapper.getUserInGroupService().add(new UserInGroup(persistedUser, group));
			} else {
				// TODO: vedere di estrarre metodo
				List<String> userGroupMemberships = userTelegramId2GroupMemberships.computeIfAbsent(persistedUser.getTelegramId(), k -> new ArrayList<>());
				if (!userGroupMemberships.contains(group.getTelegramId())) {
					boolean alreadyPersisted = servicesWrapper.getUserInGroupService().getGroupsByUserTelegramId(persistedUser.getTelegramId())
							.stream()
							.map(Group::getTelegramId)
							.anyMatch(s -> s.equals(group.getTelegramId()));
					if (!alreadyPersisted) {
						servicesWrapper.getUserInGroupService().add(new UserInGroup(persistedUser, group));
					}
					userGroupMemberships.add(group.getTelegramId());
				}
			}
			knownUsers.put(persistedUser.getTelegramId(), persistedUser);
			List<String> groupSingletonList = new ArrayList<>();
			groupSingletonList.add(group.getTelegramId());
			userTelegramId2GroupMemberships.put(persistedUser.getTelegramId(), groupSingletonList);
			cachedUser = persistedUser;
		} else {
			List<String> userGroupMemberships = userTelegramId2GroupMemberships.computeIfAbsent(cachedUser.getTelegramId(), k -> new ArrayList<>());
			if (!userGroupMemberships.contains(group.getTelegramId())) {
				boolean alreadyPersisted = servicesWrapper.getUserInGroupService().getGroupsByUserTelegramId(cachedUser.getTelegramId())
						.stream()
						.map(Group::getTelegramId)
						.anyMatch(s -> s.equals(group.getTelegramId()));
				if (!alreadyPersisted) {
					servicesWrapper.getUserInGroupService().add(new UserInGroup(cachedUser, group));
				}
				userGroupMemberships.add(group.getTelegramId());
			}
			boolean modified = false;
			if (!Objects.equals(cachedUser.getTelegramFirstName(), user.getTelegramFirstName())) {
				cachedUser.setTelegramFirstName(user.getTelegramFirstName());
				modified = true;
			}
			if (!Objects.equals(cachedUser.getTelegramLastName(), user.getTelegramLastName())) {
				cachedUser.setTelegramLastName(user.getTelegramLastName());
				modified = true;
			}
			if (!Objects.equals(cachedUser.getTelegramUsername(), user.getTelegramUsername())) {
				cachedUser.setTelegramUsername(user.getTelegramUsername());
				modified = true;
			}
			if (modified) {
				cachedUser = servicesWrapper.getUserService().add(cachedUser);
			}
		}
		return cachedUser;
	}

	private Group addKnownGroup(@NonNull Group group) {
		Group cachedGroup = knownGroups.get(group.getTelegramId());
		if (cachedGroup == null) {
			Group persistedGroup = servicesWrapper.getGroupService().getByTelegramId(group.getTelegramId());
			if (persistedGroup == null) {
				log.info("Found new group={}", group);
				persistedGroup = servicesWrapper.getGroupService().add(group);
			}
			knownGroups.put(persistedGroup.getTelegramId(), persistedGroup);
			cachedGroup = persistedGroup;
		} else {
			if (!Objects.equals(cachedGroup.getTitle(), group.getTitle())) {
				cachedGroup.setTitle(group.getTitle());
				cachedGroup = servicesWrapper.getGroupService().add(cachedGroup);
			}
		}
		return cachedGroup;
	}
}
