package net.sf.juffrou.mq.messages.task;

import net.sf.juffrou.mq.util.MessageReceivedHandler;

public interface MessageListenerTaskProvider {

	AbstractMessageListenerTask provide(MessageReceivedHandler handler, String listeningQueueName);
}
