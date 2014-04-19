package net.sf.juffrou.mq.messages.task;

import net.sf.juffrou.mq.util.MessageReceivedHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageListenerTaskFactory {

	@Autowired
	private MessageListenerTaskProvider messageListenerTaskProvider;
	
	
	public AbstractMessageListenerTask createMessageListenerTask(MessageReceivedHandler handler, String listeningQueueName) {
		return messageListenerTaskProvider.provide(handler, listeningQueueName);
	}

	public MessageListenerTaskProvider getMessageListenerTaskProvider() {
		return messageListenerTaskProvider;
	}

	public void setMessageListenerTaskProvider(
			MessageListenerTaskProvider messageListenerTaskProvider) {
		this.messageListenerTaskProvider = messageListenerTaskProvider;
	}
}
