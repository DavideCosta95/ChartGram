package chartgram.exceptions;

public class ApiCommunicationException extends Exception {
	public ApiCommunicationException() {
	}

	public ApiCommunicationException(Exception e) {
		super(e);
	}

	public ApiCommunicationException(Throwable e) {
		super(e);
	}

	public ApiCommunicationException(String message) {
		super(message);
	}

	public ApiCommunicationException(String message, Throwable cause) {
		super(message, cause);
	}
}
