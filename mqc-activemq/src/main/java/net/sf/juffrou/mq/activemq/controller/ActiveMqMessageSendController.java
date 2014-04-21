package net.sf.juffrou.mq.activemq.controller;

import java.sql.Time;

import javafx.stage.Stage;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
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

		MyMessageCreator creator = new MyMessageCreator(presenter, jmsTemplate, messageDescriptor, queueNameReceive);
		jmsTemplate.send(queueNameSend, creator);
		
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
		
		ActiveMqMessageReceivingTask task = new ActiveMqMessageReceivingTask(handler, jmsTemplate, queueNameReceive, brokerTimeout,
				creator.getMessageID(), queueNameSend);

		Thread responseReceivingThread = new Thread(task);
		responseReceivingThread.setDaemon(true);
		responseReceivingThread.start();
		
	}
	
	
	private static class MyMessageCreator implements MessageCreator {
		
		private final MessageSendPresenter presenter;
		private final JmsTemplate jmsTemplate;
		private final MessageDescriptor messageDescriptor;
		private final String replyToQueueName;
		private String messagId;
		
		public MyMessageCreator(MessageSendPresenter presenter, JmsTemplate jmsTemplate, MessageDescriptor messageDescriptor, String replyToQueueName) {
			this.presenter = presenter;
			this.jmsTemplate = jmsTemplate;
			this.messageDescriptor = messageDescriptor;
			this.replyToQueueName = replyToQueueName;
		}

		@Override
		public Message createMessage(Session session) throws JMSException {
			Message message = session.createTextMessage(messageDescriptor.getText());
			this.messagId = message.getJMSMessageID();
			
			Destination replyToQueue = jmsTemplate.getDestinationResolver().resolveDestinationName(session, replyToQueueName, false);
			message.setJMSReplyTo(replyToQueue);

			ActiveMqMessageDescriptorHelper.setMessageHeaders(message, messageDescriptor);
			
			messageDescriptor.addHeader(HeaderDescriptor.HEADER_MESSAGE_ID, message.getJMSMessageID() == null ? "" : message.getJMSMessageID());
			Long putDateTime = message.getJMSTimestamp();
			messageDescriptor.addHeader(HeaderDescriptor.HEADER_PUT_DATETIME, putDateTime == null ? "" : new Time(putDateTime).toString());

			presenter.setSentMessage(messageDescriptor);

			return message;
		}
		
		public String getMessageID() {
			return messagId;
		}
		
	}

}
