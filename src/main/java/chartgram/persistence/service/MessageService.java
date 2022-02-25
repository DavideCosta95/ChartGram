package chartgram.persistence.service;

import chartgram.persistence.entity.Message;
import chartgram.persistence.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MessageService {
	private final MessageRepository messageRepository;

	@Autowired
	private MessageService(MessageRepository messageRepository) {
		this.messageRepository = messageRepository;
	}

	public List<Message> getAll() {
		List<Message> result = new ArrayList<>();
		messageRepository.findAll().forEach(result::add);
		return result;
	}

	public List<Message> getAllByGroupTelegramId(String groupId) {
		return messageRepository.getAllByGroupTelegramId(groupId);
	}

	public long add(Message message) {
		Message persistedMessage = messageRepository.save(message);
		return persistedMessage.getId();
	}
}
