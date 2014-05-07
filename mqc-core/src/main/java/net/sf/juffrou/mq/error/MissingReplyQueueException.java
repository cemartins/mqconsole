package net.sf.juffrou.mq.error;

public class MissingReplyQueueException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9176885960905581888L;

	public MissingReplyQueueException() {
		super("Please select a response queue");
	}

	public MissingReplyQueueException(String message) {
		super(message);
	}
}
