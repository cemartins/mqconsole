package net.sf.juffrou.mq.error;

public class CannotSendMessageException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9176885960905581888L;

	public CannotSendMessageException(String message) {
		super(message);
	}

	public CannotSendMessageException(String message, Throwable t) {
		super(message, t);
	}

}
