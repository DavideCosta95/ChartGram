package chartgram.telegram;

import chartgram.model.Pair;
import chartgram.telegram.model.ITelegramBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class NullTelegramBot implements ITelegramBot {
	private static ITelegramBot instance;

	private NullTelegramBot() {}

	public static ITelegramBot getInstance() {
		if (instance == null) {
			instance = new NullTelegramBot();
		}
		return instance;
	}

	@Override
	public void addOnGroupMessageReceivedHandler(Consumer<Update> handler) {
		// no-op
	}

	@Override
	public void addOnJoiningUserHandler(Consumer<Update> handler) {
		// no-op
	}

	@Override
	public void addOnLeavingUserHandler(Consumer<Update> handler) {
		// no-op
	}

	@Override
	public void addOnPrivateMessageReceivedHandler(Consumer<Update> handler) {
		// no-op
	}

	@Override
	public void removeOnPrivateMessageReceivedHandlers() {
		// no-op
	}

	@Override
	public boolean deleteMessage(Message message) {
		return true;
	}

	@Override
	public Long getBotId() {
		return null;
	}

	@Override
	public String getBotName() {
		return null;
	}

	@Override
	public String getBotToken() {
		return null;
	}

	@Override
	public String getBotUsername() {
		return null;
	}

	@Override
	public void onUpdateReceived(Update update) {
		// no-op
	}

	@Override
	public boolean pinMessage(int messageId, String groupId) {
		return true;
	}

	@Override
	public void removeKeyboard(CallbackQuery callbackQuery) {
		// no-op
	}

	@Override
	public void removeOnGroupMessageReceivedHandlers() {
		// no-op
	}

	@Override
	public void removeOnJoiningUserHandlers() {
		// no-op
	}

	@Override
	public void removeOnLeavingUserHandlers() {
		// no-op
	}

	@Override
	public void sendMessageToAllDevelopers(String textToSend) {
		// no-op
	}

	@Override
	public void sendMessageToMultipleUsers(String textToSend, List<Long> usersIds) {
		// no-op
	}

	@Override
	public Message sendMessageToSingleChat(String textToSend, String recipientId) {
		return null;
	}

	@Override
	public Message sendMessageWithCallbackByInlineKeyboard(String textToSend, String recipientId, List<List<Pair<String, String>>> buttonsRows) {
		return null;
	}

	@Override
	public void sendMessageWithUrlByInlineKeyboard(String textToSend, String recipientId, String buttonText, String buttonUrl) {
		// no-op
	}

	@Override
	public List<Long> getAGroupAdmins(String groupId) {
		return Collections.emptyList();
	}

	@Override
	public void sendImage(InputStream image, String caption, String recipientId) {
		// no-op
	}

	@Override
	public int getGroupMembersCount(String groupId) {
		return 0;
	}
}
