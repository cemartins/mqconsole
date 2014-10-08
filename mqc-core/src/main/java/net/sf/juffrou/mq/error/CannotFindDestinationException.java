package net.sf.juffrou.mq.error;

public class CannotFindDestinationException extends RuntimeException {

	private static final long serialVersionUID = -9050044496678819303L;

	public CannotFindDestinationException(String message, Throwable cause) {
		super(message, cause);
	}

	public CannotFindDestinationException(String message) {
		super(message);
	}

	public CannotFindDestinationException(Throwable cause) {
		super(cause);
	}

}
