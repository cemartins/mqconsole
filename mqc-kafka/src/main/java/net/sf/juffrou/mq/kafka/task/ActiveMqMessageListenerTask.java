package net.sf.juffrou.mq.kafka.task;

import net.sf.juffrou.mq.activemq.util.ActiveMqMessageDescriptorHelper;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.messages.task.AbstractMessageListenerTask;
import net.sf.juffrou.mq.util.MessageReceivedHandler;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

public class ActiveMqMessageListenerTask extends AbstractMessageListenerTask {

	private final ActiveMQConnectionFactory connectionFactory;

	public ActiveMqMessageListenerTask(final MessageReceivedHandler handler, ActiveMQConnectionFactory connectionFactory, String queueNameReceive) {
		super(handler, queueNameReceive);
		this.connectionFactory = connectionFactory;
	}

	@Override
	protected MessageDescriptor call() throws Exception {

		ActiveMQConnection connection = null;
		ActiveMQSession session = null;
		MessageConsumer consumer = null;

		try {

			connection = (ActiveMQConnection) connectionFactory.createConnection();
			connection.start();

			session = (ActiveMQSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Queue receiveQueue = session.createQueue(getQueueNameReceive());

			consumer = session.createConsumer(receiveQueue);

			Message receive = consumer.receive();

			MessageDescriptor replyMessageDescriptor = ActiveMqMessageDescriptorHelper.createMessageDescriptor(receive);

			return replyMessageDescriptor;
		} finally {
			if (consumer != null)
				consumer.close();
			if (session != null)
				session.close();
			if (connection != null)
				connection.close();
		}
	}

}
