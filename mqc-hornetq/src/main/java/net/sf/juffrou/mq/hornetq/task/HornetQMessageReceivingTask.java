package net.sf.juffrou.mq.hornetq.task;

import javax.jms.Message;

import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.error.MissingReplyMessageException;
import net.sf.juffrou.mq.hornetq.util.HornetQMessageDescriptorHelper;
import net.sf.juffrou.mq.messages.task.AbstractMessageReceivingTask;
import net.sf.juffrou.mq.util.MessageReceivedHandler;

import org.springframework.jms.core.JmsTemplate;

public class HornetQMessageReceivingTask extends AbstractMessageReceivingTask {

	private final JmsTemplate jmsTemplate;
	private final String sentMessageId;

	public HornetQMessageReceivingTask(final MessageReceivedHandler handler, JmsTemplate jmsTemplate, QueueDescriptor queueNameReceive, Integer brokerTimeout, String sentMessageId, QueueDescriptor queueNameSent) {
		super(handler, queueNameReceive, brokerTimeout, queueNameSent);
		this.jmsTemplate = jmsTemplate;
		this.sentMessageId = sentMessageId;
	}

	@Override
	protected MessageDescriptor call() throws Exception {
		
		jmsTemplate.setReceiveTimeout(getBrokerTimeout());
		Message receive = jmsTemplate.receiveSelected(getQueueNameReceive().getId(), "JMSCorrelationID ='" + sentMessageId + "'");
		
		if(receive == null) {
			updateMessage("Response message not received. Timeout expired.");
			if (LOG.isDebugEnabled())
				LOG.debug("Response message not received. Timeout expired.");
			
			throw new MissingReplyMessageException("Response message not received. Timeout expired.");
		}

		MessageDescriptor replyMessageDescriptor = HornetQMessageDescriptorHelper.createMessageDescriptor(receive);

		return replyMessageDescriptor;
	}
}
