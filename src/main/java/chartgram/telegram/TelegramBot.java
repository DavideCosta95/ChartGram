package chartgram.telegram;

import chartgram.App;
import chartgram.config.Configuration;
import chartgram.exceptions.BotStartupException;
import chartgram.model.Pair;
import chartgram.telegram.model.ITelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class TelegramBot extends TelegramLongPollingBot implements ITelegramBot {
	public static final ITelegramBot Null = NullTelegramBot.getInstance();

	private String botName;
	private String botUsername;
	private String botToken;
	private Long botId;

	private final Configuration configuration;
	private final List<Consumer<Update>> onGroupMessageReceivedHandlers;
	private final List<Consumer<Update>> onPrivateMessageReceivedHandlers;
	private final List<Consumer<Update>> onJoiningUserHandlers;
	private final List<Consumer<Update>> onLeavingUserHandlers;

	public TelegramBot(Configuration configuration) throws BotStartupException {
		this.onGroupMessageReceivedHandlers = new ArrayList<>();
		this.onPrivateMessageReceivedHandlers = new ArrayList<>();
		this.onJoiningUserHandlers = new ArrayList<>();
		this.onLeavingUserHandlers = new ArrayList<>();
		this.configuration = configuration;
		loadConfiguration();
		startup();
	}

	private void startup() {
		String artifactVersion = App.getArtifactVersion();
		String messageText = "\u2B50 BOT STARTED \u2B50" + (artifactVersion == null || artifactVersion.isEmpty() ? "" : " - v" + artifactVersion);
		sendMessageToAllDevelopers(messageText);
	}


	private void loadConfiguration() throws BotStartupException {
		this.botName = configuration.getBotConfiguration().getName();
		this.botUsername = configuration.getBotConfiguration().getUsername();
		this.botToken = configuration.getBotConfiguration().getToken();
		try {
			this.botId = getMe().getId();
		} catch (TelegramApiException e) {
			throw new BotStartupException(e);
		}
		log.debug("Bot configuration successfully loaded");
	}

	@Override
	public void onUpdateReceived(Update update) {
		Message message = update.getMessage();
		if (message == null) {
			return;
		}

		Chat chat = message.getChat();

		if (chat.isGroupChat() || chat.isSuperGroupChat()) {
			handleGroupMessage(update);
		} else {
			handlePrivateMessage(update);
		}
	}

	private void handlePrivateMessage(Update update) {
		onPrivateMessageReceivedHandlers.forEach(e -> e.accept(update));
	}

	private void handleGroupMessage(Update update) {
		Message message = update.getMessage();

		if (message.getLeftChatMember() != null) {
			handleLeaveUpdate(update);
			return;
		}

		if (message.getNewChatMembers() != null && !message.getNewChatMembers().isEmpty()) {
			handleJoinUpdate(update);
			return;
		}
		onGroupMessageReceivedHandlers.forEach(e -> e.accept(update));
	}

	public List<Long> getAGroupAdmins(Long groupId) {
		GetChatAdministrators getChatAdministrators = new GetChatAdministrators();
		getChatAdministrators.setChatId(groupId.toString());
		List<ChatMember> adminsList = executeChatAction(getChatAdministrators);
		if (adminsList != null) {
			return adminsList.stream()
					.map(ChatMember::getUser)
					.map(User::getId)
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	public void sendImage(InputStream image, String caption, String recipientId) {
		SendPhoto photo = new SendPhoto();
		photo.setChatId(recipientId);
		photo.setPhoto(new InputFile(image, "chart"));
		photo.setCaption(caption);
		sendPhoto(photo);
	}

	private void handleJoinUpdate(Update update) {
		log.info("Join update received={}", update);
		onJoiningUserHandlers.forEach(e -> e.accept(update));
	}

	private void handleLeaveUpdate(Update update) {
		log.info("Leave update received={}", update);
		onLeavingUserHandlers.forEach(e -> e.accept(update));
	}

	// TODO: introdurre TelegramCommunicationController
	public Message sendMessageToSingleChat(String textToSend, String recipientId) {
		if (textToSend.trim().isBlank()) {
			return null;
		}
		log.debug("Sent message with text={} to chat with id={}", textToSend, recipientId);
		SendMessage message = new SendMessage();
		message.setText(textToSend);
		message.setChatId(recipientId);
		return sendMessage(message);
	}

	public void sendMessageToAllDevelopers(String textToSend) {
		log.debug("Sent message to all developers={}", textToSend);
		sendMessageToMultipleUsers(textToSend, configuration.getBotConfiguration().getDevelopersIds());
	}

	public void sendMessageToMultipleUsers(String textToSend, List<Long> usersIds) {
		log.debug("Sending message={} to users with id={} in progress", textToSend, usersIds);
		usersIds.forEach(id -> sendMessageToSingleChat(textToSend, id.toString()));
	}

	private void sendMessageWithInlineKeyboard(String textToSend, String recipientId, List<List<Pair<String, String>>> buttonsRows) {
		log.debug("Sent message with inline keyboard to user with telegramId={}, text={}, buttons={}", recipientId, textToSend, buttonsRows);
		SendMessage message = new SendMessage();
		message.setText(textToSend);
		message.setChatId(recipientId);

		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

		for (List<Pair<String, String>> row : buttonsRows) {
			List<InlineKeyboardButton> rowInline = new ArrayList<>();
			for (Pair<String, String> button : row) {
				rowInline.add(InlineKeyboardButton.builder().text(button.getFirst()).url(button.getSecond()).build());
			}
			rowsInline.add(rowInline);
		}

		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);
		sendMessage(message);
	}

	public void sendMessageWithUrlByInlineKeyboard(String textToSend, String recipientId, String buttonText, String buttonUrl) {
		sendMessageWithInlineKeyboard(textToSend, recipientId, List.of(List.of(new Pair<>(buttonText, buttonUrl))));
	}

	public Message sendMessageWithCallbackByInlineKeyboard(String textToSend, String recipientId, List<List<Pair<String, String>>> buttonsRows) {
		SendMessage message = new SendMessage();
		message.setText(textToSend);
		message.setChatId(recipientId);

		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

		for (List<Pair<String, String>> row : buttonsRows) {
			List<InlineKeyboardButton> rowInline = new ArrayList<>();
			for (Pair<String, String> button : row) {
				rowInline.add(InlineKeyboardButton.builder().text(button.getFirst()).callbackData(button.getSecond()).build());
			}
			rowsInline.add(rowInline);
		}

		markupInline.setKeyboard(rowsInline);
		message.setReplyMarkup(markupInline);
		return sendMessage(message);
	}

	public void removeKeyboard(CallbackQuery callbackQuery) {
		EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
		editMessageReplyMarkup.setChatId(callbackQuery.getMessage().getChatId().toString());
		editMessageReplyMarkup.setMessageId(callbackQuery.getMessage().getMessageId());
		editMessageReplyMarkup.setReplyMarkup(new InlineKeyboardMarkup());
		executeChatAction(editMessageReplyMarkup);
	}

	public boolean deleteMessage(Message message) {
		DeleteMessage deleteMessage = new DeleteMessage();
		deleteMessage.setMessageId(message.getMessageId());
		deleteMessage.setChatId(message.getChatId().toString());
		return Boolean.TRUE.equals(executeChatAction(deleteMessage));
	}

	private Message sendMessage(SendMessage message) {
		Message messageSent = executeChatAction(message);
		boolean messageSentSuccessfully = messageSent != null;
		if (!messageSentSuccessfully) {
			log.debug("Failed sending message={}", message);
		}
		return messageSent;
	}

	public boolean pinMessage(int messageId, String groupId) {
		PinChatMessage pinChatMessage = new PinChatMessage();
		pinChatMessage.setChatId(groupId);
		pinChatMessage.setMessageId(messageId);
		return executeChatAction(pinChatMessage) != null;
	}

	private Message sendPhoto(SendPhoto photo) {
		try {
			Message result = execute(photo);
			Thread.sleep(80L);
			return result;
		} catch (TelegramApiException e) {
			log.error("", e);
		} catch (InterruptedException e) {
			log.error("", e);
			Thread.currentThread().interrupt();
		}
		return null;
	}

	private <T extends Serializable, A extends BotApiMethod<T>> T executeChatAction(A action) {
		try {
			T result = execute(action);
			Thread.sleep(80L);
			return result;
		} catch (TelegramApiException e) {
			log.error("", e);
		} catch (InterruptedException e) {
			log.error("", e);
			Thread.currentThread().interrupt();
		}
		return null;
	}

	public void addOnGroupMessageReceivedHandler(Consumer<Update> handler) {
		this.onGroupMessageReceivedHandlers.add(handler);
	}

	public void removeOnGroupMessageReceivedHandlers() {
		this.onGroupMessageReceivedHandlers.clear();
	}

	public void addOnPrivateMessageReceivedHandler(Consumer<Update> handler) {
		this.onPrivateMessageReceivedHandlers.add(handler);
	}

	public void removeOnPrivateMessageReceivedHandlers() {
		this.onPrivateMessageReceivedHandlers.clear();
	}

	public void addOnJoiningUserHandler(Consumer<Update> handler) {
		this.onJoiningUserHandlers.add(handler);
	}

	public void removeOnJoiningUserHandlers() {
		this.onJoiningUserHandlers.clear();
	}

	public void addOnLeavingUserHandler(Consumer<Update> handler) {
		this.onLeavingUserHandlers.add(handler);
	}

	public void removeOnLeavingUserHandlers() {
		this.onLeavingUserHandlers.clear();
	}

	public String getBotName() {
		return botName;
	}

	@Override
	public String getBotUsername() {
		return botUsername;
	}

	@Override
	public String getBotToken() {
		return botToken;
	}

	public Long getBotId() {
		return botId;
	}
}
