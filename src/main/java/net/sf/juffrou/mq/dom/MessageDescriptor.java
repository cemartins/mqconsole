package net.sf.juffrou.mq.dom;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class MessageDescriptor {

	private static final int SHORT_TEXT_LEN = 160;

	private String messageId;
	private String correlationId;
	private String text;
	private final Map<String, HeaderDescriptor> headers = new TreeMap<String, HeaderDescriptor>();

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public String getShortText() {
		if (text != null && text.length() > SHORT_TEXT_LEN)
			return text.substring(0, SHORT_TEXT_LEN) + "...";
		else
			return text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Collection<HeaderDescriptor> getHeaders() {
		return headers.values();
	}

	public void addHeader(String key, Object value) {
		headers.put(key, new HeaderDescriptor(key, value));
	}

	public void addHeader(HeaderDescriptor headerDescriptor) {
		headers.put(headerDescriptor.getName(), headerDescriptor);
	}

	public Object getHeader(String key) {
		return headers.get(key) != null ? headers.get(key).getValue() : null;
	}

}
