package net.sf.juffrou.mq.activemq.task;

import javax.jms.Message;

import net.sf.juffrou.mq.activemq.util.ActiveMqMessageDescriptorHelper;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.messages.task.AbstractMessageListenerTask;
import net.sf.juffrou.mq.util.MessageReceivedHandler;

import org.springframework.jms.core.JmsTemplate;

public class ActiveMqMessageListenerTask extends AbstractMessageListenerTask {

	private final JmsTemplate jmsTemplate;

	public ActiveMqMessageListenerTask(final MessageReceivedHandler handler, JmsTemplate jmsTemplate, String queueNameReceive) {
		super(handler, queueNameReceive);
		this.jmsTemplate = jmsTemplate;
	}


	@Override
	protected MessageDescriptor call() throws Exception {
		
		
		Message receive = jmsTemplate.receive(getQueueNameReceive());

		MessageDescriptor replyMessageDescriptor = ActiveMqMessageDescriptorHelper.createMessageDescriptor(receive);

		return replyMessageDescriptor;
	}

}
