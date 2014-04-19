package net.sf.juffrou.mq.websphere.controller;

import net.sf.juffrou.mq.messages.task.AbstractMessageListenerTask;
import net.sf.juffrou.mq.messages.task.MessageListenerTaskProvider;
import net.sf.juffrou.mq.util.MessageReceivedHandler;
import net.sf.juffrou.mq.websphere.task.MessageListenerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.ibm.mq.MQQueueManager;

@Component
public class WebsphereMessageListenerTaskProvider implements MessageListenerTaskProvider {

	@Autowired
	@Qualifier("mqListeningQueueManager")
	private MQQueueManager qm;

	@Override
	public AbstractMessageListenerTask provide(MessageReceivedHandler handler, String listeningQueueName) {
		return new MessageListenerTask(handler, qm, listeningQueueName);
	}

}
