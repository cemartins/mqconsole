package net.sf.juffrou.mq.kafka.task;

import net.sf.juffrou.mq.activemq.util.ActiveMqMessageDescriptorHelper;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.error.MissingReplyMessageException;
import net.sf.juffrou.mq.messages.task.AbstractMessageReceivingTask;
import net.sf.juffrou.mq.util.MessageReceivedHandler;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQSession;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

public class ActiveMqMessageReceivingTask extends AbstractMessageReceivingTask {

	private final ActiveMQConnection connection;
	private final ActiveMQSession session;
	private final MessageConsumer consumer;

	public ActiveMqMessageReceivingTask(final MessageReceivedHandler handler, ActiveMQConnection connection, ActiveMQSession session, MessageConsumer consumer, QueueDescriptor queueNameReceive, Integer brokerTimeout, QueueDescriptor queueNameSent) {
		super(handler, queueNameReceive, brokerTimeout, queueNameSent);
		this.connection = connection;
		this.session = session;
		this.consumer = consumer;
	}

	@Override
	protected MessageDescriptor call() throws Exception {

		Message receive = null;

		try {

			receive = consumer.receive(getBrokerTimeout());

		} catch (JMSException e) {
			throw new MissingReplyMessageException("Message reception task failed.", e);
		} finally {
			if (consumer != null)
				consumer.close();
			if (session != null)
				session.close();
			if (connection != null)
				connection.close();
		}

		if (receive == null) {
			updateMessage("Response message not received. Timeout expired.");
			if (LOG.isDebugEnabled())
				LOG.debug("Response message not received. Timeout expired.");

			throw new MissingReplyMessageException("Response message not received. Timeout expired.");
		}

		MessageDescriptor replyMessageDescriptor = ActiveMqMessageDescriptorHelper.createMessageDescriptor(receive);

		return replyMessageDescriptor;
	}
}
