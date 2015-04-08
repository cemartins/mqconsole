package net.sf.juffrou.mq.error;

public class BrokerSpecificException extends Exception {

	private static final long serialVersionUID = -8962450505109154915L;


	public BrokerSpecificException(String message, Throwable cause) {
		super(message, cause);
	}

	public BrokerSpecificException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
