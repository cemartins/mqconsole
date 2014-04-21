package net.sf.juffrou.mq.messages;

import net.sf.juffrou.mq.dom.MessageDescriptor;

public interface MessageViewController {

	void setMessageDescriptor(MessageDescriptor messageDescriptor);
	
	void initialize();
}
