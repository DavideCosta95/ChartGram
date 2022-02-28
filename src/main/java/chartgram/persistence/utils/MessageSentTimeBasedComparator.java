package chartgram.persistence.utils;

import chartgram.persistence.entity.Message;

import java.util.Comparator;

public class MessageSentTimeBasedComparator implements Comparator<Message> {
	@Override
	public int compare(Message m1, Message m2) {
		return m1.getSentAt().compareTo(m2.getSentAt());
	}
}
