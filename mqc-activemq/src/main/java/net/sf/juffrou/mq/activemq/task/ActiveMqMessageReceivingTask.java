package net.sf.juffrou.mq.activemq.task;

import javax.jms.Message;

import net.sf.juffrou.mq.activemq.util.ActiveMqMessageDescriptorHelper;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.messages.task.AbstractMessageReceivingTask;
import net.sf.juffrou.mq.util.MessageReceivedHandler;

import org.springframework.jms.core.JmsTemplate;

public class ActiveMqMessageReceivingTask extends AbstractMessageReceivingTask {

	private final JmsTemplate jmsTemplate;
	private final String sentMessageId;

	public ActiveMqMessageReceivingTask(final MessageReceivedHandler handler, JmsTemplate jmsTemplate, String queueNameReceive, Integer brokerTimeout, String sentMessageId, String queueNameSent) {
		super(handler, queueNameReceive, brokerTimeout, queueNameSent);
		this.jmsTemplate = jmsTemplate;
		this.sentMessageId = sentMessageId;
	}

	@Override
	protected MessageDescriptor call() throws Exception {
		
		jmsTemplate.setReceiveTimeout(getBrokerTimeout());
		Message receive = jmsTemplate.receiveSelected(getQueueNameReceive(), "Correlation ID=" + sentMessageId);

		MessageDescriptor replyMessageDescriptor = ActiveMqMessageDescriptorHelper.createMessageDescriptor(receive);

		return replyMessageDescriptor;
	}
}
