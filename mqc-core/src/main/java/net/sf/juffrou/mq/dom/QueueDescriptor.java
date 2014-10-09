package net.sf.juffrou.mq.dom;

/**
 * @author cemartins
 *
 */
public class QueueDescriptor {

	private String id;
	private String name;
	private String description;
	private Long dept;
	private Boolean isSherable;
	
	public QueueDescriptor() {
		isSherable = Boolean.FALSE;
	}
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public Long getDept() {
		return dept;
	}

	public void setDept(Long dept) {
		this.dept = dept;
	}


	public Boolean getIsSherable() {
		return isSherable;
	}


	public void setIsSherable(Boolean isSherable) {
		this.isSherable = isSherable;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(! (obj instanceof QueueDescriptor) )
			return false;
		QueueDescriptor other = (QueueDescriptor) obj;
		if(other.getId() == null)
			return false;
		return other.getId().equals(getId());
	}
}
