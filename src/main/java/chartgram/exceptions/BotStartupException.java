package chartgram.exceptions;

public class BotStartupException extends Exception {
	public BotStartupException() {
	}

	public BotStartupException(Exception e) {
		super(e);
	}

	public BotStartupException(String message) {
		super(message);
	}

	public BotStartupException(String message, Throwable cause) {
		super(message, cause);
	}
}
