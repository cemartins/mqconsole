package net.sf.juffrou.mq.error;

public class MissingReplyMessageException extends RuntimeException {

	private static final long serialVersionUID = -9050044496678819303L;

	public MissingReplyMessageException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingReplyMessageException(String message) {
		super(message);
	}

	public MissingReplyMessageException(Throwable cause) {
		super(cause);
	}

}
