package net.sf.juffrou.mq.error;

public class FXMLLoadingError extends RuntimeException {

	private static final long serialVersionUID = 8887685314582848817L;

	public FXMLLoadingError() {
		super();
	}

	public FXMLLoadingError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FXMLLoadingError(String message, Throwable cause) {
		super(message, cause);
	}

	public FXMLLoadingError(String message) {
		super(message);
	}

	public FXMLLoadingError(Throwable cause) {
		super(cause);
	}

}
