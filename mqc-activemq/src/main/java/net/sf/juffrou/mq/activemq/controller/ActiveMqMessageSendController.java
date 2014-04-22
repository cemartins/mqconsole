package net.sf.juffrou.mq.activemq.controller;

import java.sql.Time;

import javafx.stage.Stage;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import net.sf.juffrou.mq.activemq.task.ActiveMqMessageReceivingTask;
import net.sf.juffrou.mq.activemq.util.ActiveMqMessageDescriptorHelper;
import net.sf.juffrou.mq.dom.HeaderDescriptor;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.messages.MessageSendController;
import net.sf.juffrou.mq.messages.presenter.MessageSendPresenter;
import net.sf.juffrou.mq.util.MessageReceivedHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.ProducerCallback;
import org.springframework.stereotype.Component;

@Component
public class ActiveMqMessageSendController implements MessageSendController {

	protected static final Logger LOG = LoggerFactory.getLogger(ActiveMqMessageSendController.class);

	@Autowired
	private JmsTemplate jmsTemplate;

	@Value("${broker_timeout}")
	private Integer brokerTimeout;

	// This method called to send MQ message to the norma messaging server
	// RECEIVES a message STRING and returns a message object (used as a
	// reference for the reply)
	public void sendMessage(MessageSendPresenter presenter, MessageDescriptor messageDescriptor, String queueNameSend, String queueNameReceive) {

		MyMessageProducer creator = new MyMessageProducer(presenter, jmsTemplate, messageDescriptor, queueNameReceive, brokerTimeout);
		String sendMessageId = jmsTemplate.execute(queueNameSend, creator);
		
		if(sendMessageId == null) {
			
			// user did not choose a reply queue, so the sending mechanism created a temporary one and waited for the reception
			Message replyMessage = creator.getReplyMessage();
			try {
				MessageDescriptor replyMessageDescriptor = ActiveMqMessageDescriptorHelper.createMessageDescriptor(replyMessage);
				presenter.displayMessageReceived(replyMessageDescriptor);
			} catch (JMSException e) {
				LOG.error("Cannot create MessageDescriptor from received message", e);
			}
		}
		else {

			// activate the receiving thread
			MessageReceivedHandler handler = new MessageReceivedHandler() {
				@Override
				public void messageReceived(MessageDescriptor messageDescriptor) {
					presenter.displayMessageReceived(messageDescriptor);
				}

				@Override
				public Stage getStage() {
					return presenter.getStage();
				}
			};
			
			ActiveMqMessageReceivingTask task = new ActiveMqMessageReceivingTask(handler, jmsTemplate, queueNameReceive, brokerTimeout,	sendMessageId, queueNameSend);

			Thread responseReceivingThread = new Thread(task);
			responseReceivingThread.setDaemon(true);
			responseReceivingThread.start();
		}
		
	}
	
	private static class MyMessageProducer implements ProducerCallback<String> {
		private final MessageSendPresenter presenter;
		private final JmsTemplate jmsTemplate;
		private final MessageDescriptor messageDescriptor;
		private final String replyToQueueName;
		private final Integer brokerTimeout;
		private Message replyMessage;
		
		public MyMessageProducer(MessageSendPresenter presenter, JmsTemplate jmsTemplate, MessageDescriptor messageDescriptor, String replyToQueueName, Integer brokerTimeout) {
			this.presenter = presenter;
			this.jmsTemplate = jmsTemplate;
			this.messageDescriptor = messageDescriptor;
			this.replyToQueueName = replyToQueueName;
			this.brokerTimeout = brokerTimeout;
		}

		@Override
		public String doInJms(Session session, MessageProducer producer) throws JMSException {

			Message message = session.createTextMessage(messageDescriptor.getText());
			boolean responseOnTempQueue = false;
			
			Destination replyToQueue;
			if(replyToQueueName != null && !replyToQueueName.isEmpty())
				replyToQueue = jmsTemplate.getDestinationResolver().resolveDestinationName(session, replyToQueueName, false);
			else {
				replyToQueue = session.createTemporaryQueue();
				responseOnTempQueue = true;
			}
			
			message.setJMSReplyTo(replyToQueue);

			ActiveMqMessageDescriptorHelper.setMessageHeaders(message, messageDescriptor);

			producer.setDisableMessageID(false);
			producer.send(message);

			messageDescriptor.addHeader(HeaderDescriptor.HEADER_MESSAGE_ID, message.getJMSMessageID() == null ? "" : message.getJMSMessageID());
			Long putDateTime = message.getJMSTimestamp();
			messageDescriptor.addHeader(HeaderDescriptor.HEADER_PUT_DATETIME, putDateTime == null ? "" : new Time(putDateTime).toString());

			presenter.setSentMessage(messageDescriptor);
			
			if(responseOnTempQueue) {
				MessageConsumer consumer = session.createConsumer(replyToQueue);
				replyMessage = consumer.receive(brokerTimeout);
				return null;
			}

			return message.getJMSMessageID();
		}
		
		public Message getReplyMessage() {
			return replyMessage;
		}
	
	}
	
}
