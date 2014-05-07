package net.sf.juffrou.mq.messages;

import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.error.MissingReplyQueueException;
import net.sf.juffrou.mq.messages.presenter.MessageSendPresenter;

public interface MessageSendController {

	void sendMessage(MessageSendPresenter presenter, MessageDescriptor messageDescriptor, String queueNameSend, String queueNameReceive) throws MissingReplyQueueException;
}
