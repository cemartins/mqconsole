package net.sf.juffrou.mq.hornetq.task;

import javax.jms.Message;

import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.hornetq.util.HornetQMessageDescriptorHelper;
import net.sf.juffrou.mq.messages.task.AbstractMessageListenerTask;
import net.sf.juffrou.mq.util.MessageReceivedHandler;

import org.springframework.jms.core.JmsTemplate;

public class HornetQMessageListenerTask extends AbstractMessageListenerTask {

	private final JmsTemplate jmsTemplate;

	public HornetQMessageListenerTask(final MessageReceivedHandler handler, JmsTemplate jmsTemplate, String queueNameReceive) {
		super(handler, queueNameReceive);
		this.jmsTemplate = jmsTemplate;
	}


	@Override
	protected MessageDescriptor call() throws Exception {
		
		
		jmsTemplate.setReceiveTimeout(jmsTemplate.RECEIVE_TIMEOUT_INDEFINITE_WAIT);
		Message receive = jmsTemplate.receive(getQueueNameReceive());

		MessageDescriptor replyMessageDescriptor = HornetQMessageDescriptorHelper.createMessageDescriptor(receive);

		return replyMessageDescriptor;
	}

}
