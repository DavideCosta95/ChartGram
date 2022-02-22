package chartgram.telegram;

import chartgram.model.Pair;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.function.Consumer;

public interface ITelegramBot {
	void addOnGroupMessageReceivedHandler(Consumer<Update> handler);
	void addOnJoiningUserHandler(Consumer<Update> handler);
	void addOnLeavingUserHandler(Consumer<Update> handler);
	boolean deleteMessage(Message message);
	Long getBotId();
	String getBotName();
	String getBotToken();
	String getBotUsername();
	void onUpdateReceived(Update update);
	boolean pinMessage(int messageId, String groupId);
	void removeKeyboard(CallbackQuery callbackQuery);
	void removeOnGroupMessageReceivedHandlers();
	void removeOnJoiningUserHandlers();
	void removeOnLeavingUserHandlers();
	void sendMessageToAllDevelopers(String textToSend);
	void sendMessageToAllOwners(String textToSend);
	void sendMessageToMultipleUsers(String textToSend, List<Long> usersIds);
	Message sendMessageToSingleChat(String textToSend, String recipientId);
	Message sendMessageWithCallbackByInlineKeyboard(String textToSend, String recipientId, List<List<Pair<String, String>>> buttonsRows);
	void sendMessageWithUrlByInlineKeyboard(String textToSend, String recipientId, String buttonText, String buttonUrl);
}
