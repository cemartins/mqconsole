package net.sf.juffrou.mq.messages;

import net.sf.juffrou.mq.dom.MessageDescriptor;

public interface MessageViewPresenter {

	void setMessageDescriptor(MessageDescriptor messageDescriptor);
	
	void initialize();
}
