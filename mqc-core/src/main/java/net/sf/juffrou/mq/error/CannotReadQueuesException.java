package net.sf.juffrou.mq.error;

public class CannotReadQueuesException extends RuntimeException {

	private static final long serialVersionUID = -9050044496678819303L;

	public CannotReadQueuesException(String message, Throwable cause) {
		super(message, cause);
	}

	public CannotReadQueuesException(String message) {
		super(message);
	}

	public CannotReadQueuesException(Throwable cause) {
		super(cause);
	}

}
