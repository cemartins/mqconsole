package net.sf.juffrou.mq.dom;

public class HeaderDescriptor {

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
		return value.toString();
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
