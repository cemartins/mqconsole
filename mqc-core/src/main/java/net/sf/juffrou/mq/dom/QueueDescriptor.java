package net.sf.juffrou.mq.dom;

public class QueueDescriptor {

	private String name;
	private String description;
	private Integer dept;
	private Boolean isSherable;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getDept() {
		return dept;
	}

	public void setDept(Integer dept) {
		this.dept = dept;
	}

	public Boolean getIsSherable() {
		return isSherable;
	}

	public void setIsSherable(Boolean isSherable) {
		this.isSherable = isSherable;
	}
	
}
