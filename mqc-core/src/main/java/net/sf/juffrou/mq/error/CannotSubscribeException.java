package net.sf.juffrou.mq.error;

public class CannotSubscribeException extends RuntimeException {

	private static final long serialVersionUID = -9050044496678819303L;

	public CannotSubscribeException(String message, Throwable cause) {
		super(message, cause);
	}

	public CannotSubscribeException(String message) {
		super(message);
	}

	public CannotSubscribeException(Throwable cause) {
		super(cause);
	}

}
