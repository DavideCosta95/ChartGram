package chartgram.telegram.model;

import java.util.Arrays;

public enum MessageType {
	TEXT(1),
	AUDIO(2),
	PHOTO(3),
	STICKER(4),
	VIDEO(5),
	GIF(6),
	OTHER(7);

	private final int id;
	MessageType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static MessageType getTypeById(int id) {
		return Arrays.stream(values()).filter(e -> id == e.getId()).findAny().orElse(OTHER);
	}
}
