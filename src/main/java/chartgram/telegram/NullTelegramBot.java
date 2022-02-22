package chartgram.telegram;

import chartgram.model.Pair;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.function.Consumer;

public class NullTelegramBot implements ITelegramBot {
	@Override
	public void addOnGroupMessageReceivedHandler(Consumer<Update> handler) {

	}

	@Override
	public void addOnJoiningUserHandler(Consumer<Update> handler) {

	}

	@Override
	public void addOnLeavingUserHandler(Consumer<Update> handler) {

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

	}

	@Override
	public boolean pinMessage(int messageId, String groupId) {
		return true;
	}

	@Override
	public void removeKeyboard(CallbackQuery callbackQuery) {

	}

	@Override
	public void removeOnGroupMessageReceivedHandlers() {

	}

	@Override
	public void removeOnJoiningUserHandlers() {

	}

	@Override
	public void removeOnLeavingUserHandlers() {

	}

	@Override
	public void sendMessageToAllDevelopers(String textToSend) {

	}

	@Override
	public void sendMessageToAllOwners(String textToSend) {

	}

	@Override
	public void sendMessageToMultipleUsers(String textToSend, List<Long> usersIds) {

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

	}
}
