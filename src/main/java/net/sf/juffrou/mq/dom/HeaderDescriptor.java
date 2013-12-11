package net.sf.juffrou.mq.dom;

public class HeaderDescriptor {
	
	public static final String HEADER_MESSAGE_ID = "MessageId";
	public static final String HEADER_CORRELATION_ID = "CorrelationId";
	public static final String HEADER_PUT_DATETIME = "PutDateTime";
	public static final String HEADER_SEQUENCE_NUMBER = "SEQUENCE_NUMBER";
	public static final String HEADER_SEQUENCE_SIZE = "SEQUENCE_SIZE";
	public static final String HEADER_EXPIRATION_DATE = "EXPIRATION_DATE";

	private String name;
	private Object value;

	public HeaderDescriptor() {
	}

	public HeaderDescriptor(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public String getValueAsString() {
		return value != null ? value.toString() : "";
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
