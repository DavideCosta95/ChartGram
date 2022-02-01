package chartgram.telegram;

import chartgram.App;
import chartgram.config.Configuration;
import chartgram.config.Language;
import chartgram.config.Localization;
import chartgram.exceptions.BotStartupException;
import chartgram.model.Pair;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
	private String botName;
	private String botUsername;
	private String botToken;
	private Long botId;

	private final Configuration configuration;
	private final Language language;
	private final List<Consumer<Update>> onGroupMessageReceivedHandlers;

	public TelegramBot(Configuration configuration, Localization localization) throws BotStartupException {
		this.onGroupMessageReceivedHandlers = new ArrayList<>();
		this.configuration = configuration;
		String languageName = configuration.getLanguage();
		this.language = localization.getLanguage(languageName);
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
		if (update.hasCallbackQuery()) {
			handleCallbackQuery(update);
			return;
		}

		Message message = update.getMessage();
		if (message == null) {
			return;
		}

		Chat chat = message.getChat();
		// TODO: test
		onGroupMessageReceivedHandlers.forEach(e -> e.accept(update));

		if (chat.isGroupChat() || chat.isSuperGroupChat()) {
			//onGroupMessageReceivedHandlers.forEach(e -> e.accept(update));
		} else {
			handlePrivateMessage(update);
		}
	}

	private void handleCallbackQuery(Update update) {
		// TODO
	}

	private void handlePrivateMessage(Update update) {
		Message message = update.getMessage();
		User sender = message.getFrom();

		if (message.hasText()) {
			handleTextUpdate(update);
		} else {
			handleNonTextUpdate(update);
		}

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

		User sender = message.getFrom();

		if (Boolean.TRUE.equals(sender.getIsBot())) {
			return;
		}

		TelegramUser telegramUser = new TelegramUser(sender.getId(), sender.getFirstName(), sender.getLastName(), sender.getUserName());

		// TODO
		// addSeenUser(telegramUser);
	}

	private void addSeenUser(TelegramUser telegramUser) {
		// TODO
	}


	private void handleJoinUpdate(Update update) {
		// TODO
	}

	private void handleLeaveUpdate(Update update) {
		// TODO
	}

	private void handleNonTextUpdate(Update update) {
		Chat chat = update.getMessage().getChat();
		boolean ignoreNonCommandMessages = configuration.getBotConfiguration().getIgnoreNonCommandsMessages();

		if (!ignoreNonCommandMessages) {
			sendMessageToSingleChat(language.getNonCommandText(), chat.getId().toString());
		}
	}

	private void handleTextUpdate(Update update) {
		User sender = update.getMessage().getFrom();
		TelegramUser telegramUser = new TelegramUser(sender.getId(), sender.getFirstName(), sender.getLastName(), sender.getUserName());
		Chat chat = update.getMessage().getChat();
		String receivedText = update.getMessage().getText();
		log.info("Sender={} - received text={}", sender, receivedText);

		boolean ignoreNonCommandMessages = configuration.getBotConfiguration().getIgnoreNonCommandsMessages();

		if (receivedText.startsWith("/")) {
			handleCommand(update);
		} else if (Boolean.FALSE.equals(ignoreNonCommandMessages)) {
			sendMessageToSingleChat(language.getNonCommandText(), chat.getId().toString());
		}
	}

	private void handleCommand(Update update) {
		Message receivedMessage = update.getMessage();
		String receivedText = receivedMessage.getText();
		// TODO
		//commandFactory.getCommandByString(receivedText).run(update);
	}

	public void answerCallbackQuery(String callbackQueryId) {
		AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
		answerCallbackQuery.setCallbackQueryId(callbackQueryId);
		executeChatAction(answerCallbackQuery);
	}

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

	public void sendMessageToAllOwners(String textToSend) {
		log.debug("Sent message to all owners={}", textToSend);
		sendMessageToMultipleUsers(textToSend, configuration.getBotConfiguration().getOwnersIds());
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

	// TODO: vedere equals
	public void removeOnGroupMessageReceivedHandler(Consumer<Update> handler) {
		this.onGroupMessageReceivedHandlers.remove(handler);
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
