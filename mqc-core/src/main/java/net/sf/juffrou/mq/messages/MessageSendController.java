package net.sf.juffrou.mq.messages;

import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.error.CannotSendMessageException;
import net.sf.juffrou.mq.messages.presenter.MessageSendPresenter;

/**
 * @author cemartins
 *
 */
public interface MessageSendController {

	/**
	 * Sends a message to a destination channel
	 * @param presenter The caller javafx controller
	 * @param messageDescriptor The contents of the message
	 * @param queueSend QueueDescriptor of the destination (queue or topic) to send the message to
	 * @param hasReply True if message is request-reply or false if no reply
	 * @param queueReceive QueueDescriptor of the destination where the reply message will be delivered (hasReply=true)
	 * @throws CannotSendMessageException
	 */
	void sendMessage(MessageSendPresenter presenter, MessageDescriptor messageDescriptor, QueueDescriptor queueSend, Boolean hasReply, QueueDescriptor queueReceive) throws CannotSendMessageException;
}
