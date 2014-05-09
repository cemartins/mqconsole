package net.sf.juffrou.mq.activemq.controller;

import java.sql.Time;

import javafx.stage.Stage;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import net.sf.juffrou.mq.activemq.task.ActiveMqMessageReceivingTask;
import net.sf.juffrou.mq.activemq.util.ActiveMqMessageDescriptorHelper;
import net.sf.juffrou.mq.dom.HeaderDescriptor;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.error.CannotSendMessageException;
import net.sf.juffrou.mq.messages.MessageSendController;
import net.sf.juffrou.mq.messages.presenter.MessageSendPresenter;
import net.sf.juffrou.mq.util.MessageReceivedHandler;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class ActiveMqMessageSendController implements MessageSendController {

	protected static final Logger LOG = LoggerFactory.getLogger(ActiveMqMessageSendController.class);

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private ActiveMQConnectionFactory connectionFactory;

	@Value("${broker_timeout}")
	private Integer brokerTimeout;

	// This method called to send MQ message to the norma messaging server
	// RECEIVES a message STRING and returns a message object (used as a
	// reference for the reply)
	@Override
	public void sendMessage(MessageSendPresenter presenter, MessageDescriptor messageDescriptor, String queueNameSend, String queueNameReceive) throws CannotSendMessageException {

		ActiveMQConnection connection = null;
		ActiveMQSession session = null;
		MessageProducer producer = null;
		MessageConsumer consumer = null;

		// handler for the receiving thread
		MessageReceivedHandler handler = new MyMessageReceivedHandler(presenter);

		try {

			connection = (ActiveMQConnection) connectionFactory.createConnection();
			connection.start();

			session = (ActiveMQSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Destination sendDestination = jmsTemplate.getDestinationResolver().resolveDestinationName(session, queueNameSend, false);

			TextMessage message = session.createTextMessage(messageDescriptor.getText());
			ActiveMqMessageDescriptorHelper.setMessageHeaders(message, messageDescriptor);

			Destination replyDestination = null;

			if (queueNameReceive != null)
				replyDestination = jmsTemplate.getDestinationResolver().resolveDestinationName(session, queueNameReceive, false);
			if (replyDestination == null) {
				replyDestination = session.createTemporaryQueue();
			}

			producer = session.createProducer(sendDestination);

			// send the message
			message.setJMSReplyTo(replyDestination);

			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			producer.setDisableMessageID(false);
			producer.send(sendDestination, message);

			String jmsMessageID = message.getJMSMessageID();

			// create the consumer task
			consumer = session.createConsumer(replyDestination, "JMSCorrelationID = '" + jmsMessageID + "'");
			ActiveMqMessageReceivingTask task = new ActiveMqMessageReceivingTask(handler, connection, session,
					consumer, queueNameReceive, brokerTimeout, queueNameSend);

			// activate the consumer task thread
			Thread responseReceivingThread = new Thread(task);
			responseReceivingThread.setDaemon(true);

			// get the response in a different thread
			responseReceivingThread.start();

			// display the updated Sent Message Descriptor on the presenter window
			messageDescriptor.addHeader(HeaderDescriptor.HEADER_MESSAGE_ID, message.getJMSMessageID() == null ? ""
					: message.getJMSMessageID());
			Long putDateTime = message.getJMSTimestamp();
			messageDescriptor.addHeader(HeaderDescriptor.HEADER_PUT_DATETIME, putDateTime == null ? "" : new Time(
					putDateTime).toString());

			presenter.setSentMessage(messageDescriptor);

		} catch (Exception e) {
			try {
				if (consumer != null)
					consumer.close();
				if (session != null)
					session.close();
				if (connection != null)
					connection.close();
			} catch (JMSException ee) {
			}
			throw new CannotSendMessageException("Message sending failed.", e);
		} finally {
			try {
				if (producer != null)
					producer.close();
			} catch (Exception e) {
			}
		}
	}

	private static class MyMessageReceivedHandler implements MessageReceivedHandler {

		private final MessageSendPresenter presenter;

		public MyMessageReceivedHandler(MessageSendPresenter presenter) {
			this.presenter = presenter;
		}

		@Override
		public void messageReceived(MessageDescriptor messageDescriptor) {
			presenter.displayMessageReceived(messageDescriptor);
		}

		@Override
		public Stage getStage() {
			return presenter.getStage();
		}

	}
}
