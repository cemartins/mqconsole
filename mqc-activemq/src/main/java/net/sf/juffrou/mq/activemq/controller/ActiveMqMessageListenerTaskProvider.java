package net.sf.juffrou.mq.activemq.controller;

import net.sf.juffrou.mq.activemq.task.ActiveMqMessageListenerTask;
import net.sf.juffrou.mq.messages.task.AbstractMessageListenerTask;
import net.sf.juffrou.mq.messages.task.MessageListenerTaskProvider;
import net.sf.juffrou.mq.util.MessageReceivedHandler;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActiveMqMessageListenerTaskProvider implements MessageListenerTaskProvider {

	@Autowired
	private ActiveMQConnectionFactory connectionFactory;

	@Override
	public AbstractMessageListenerTask provide(MessageReceivedHandler handler, String listeningQueueName) {
		return new ActiveMqMessageListenerTask(handler, connectionFactory, listeningQueueName);
	}
	
}
