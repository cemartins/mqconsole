package net.sf.juffrou.mq.dom;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author cemartins
 *
 */
public class QueueDescriptor {

	private String id;
	private StringProperty name;
	private StringProperty description;
	private LongProperty dept;
	private Boolean isSherable;
	
	public QueueDescriptor() {
		name = new SimpleStringProperty();
		description = new SimpleStringProperty();
		dept = new SimpleLongProperty();
		isSherable = Boolean.FALSE;
	}
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}
	
	public StringProperty nameProperty() {
		return name;
	}

	public String getDescription() {
		return description.get();
	}

	public void setDescription(String description) {
		this.description.set(description);
	}
	
	public StringProperty descriptionProperty() {
		return description;
	}

	public Long getDept() {
		return dept.get();
	}

	public void setDept(Long dept) {
		this.dept.set(dept);
	}

	public LongProperty deptProperty() {
		return dept;
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
