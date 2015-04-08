package net.sf.juffrou.mq.websphere.controller;

import java.util.GregorianCalendar;

import javafx.stage.Stage;
import net.sf.juffrou.mq.dom.HeaderDescriptor;
import net.sf.juffrou.mq.dom.MessageDescriptor;
import net.sf.juffrou.mq.dom.QueueDescriptor;
import net.sf.juffrou.mq.error.CannotSendMessageException;
import net.sf.juffrou.mq.error.MissingReplyQueueException;
import net.sf.juffrou.mq.messages.MessageSendController;
import net.sf.juffrou.mq.messages.presenter.MessageSendPresenter;
import net.sf.juffrou.mq.util.MessageReceivedHandler;
import net.sf.juffrou.mq.websphere.task.WebsphereMessageReceivingTask;
import net.sf.juffrou.mq.websphere.util.MessageDescriptorHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.PCFConstants;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WebsphereMessageSendController implements MessageSendController {

	protected static final Logger LOG = LoggerFactory.getLogger(WebsphereMessageSendController.class);

	@Autowired
	@Qualifier("mqQueueManager")
	private MQQueueManager qm;

	@Value("${broker_timeout}")
	private Integer brokerTimeout;

	// This method called to send MQ message to the norma messaging server
	// RECEIVES a message STRING and returns a message object (used as a
	// reference for the reply)
	public void sendMessage(MessageSendPresenter presenter, MessageDescriptor messageDescriptor, QueueDescriptor queueNameSend, Boolean hasReply, QueueDescriptor queueNameReceive) throws MissingReplyQueueException, CannotSendMessageException {

		if (hasReply && queueNameReceive == null)
			throw new MissingReplyQueueException();

		MQQueue requestQueue = null;
		try {
			MQMessage sendMessage = null;

			try {
				int openOptions;

				// If the name of the request queue is the same as the reply
				// queue...
				if (hasReply && queueNameSend.equals(queueNameReceive)) {
					openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT;
				} else {
					openOptions = MQConstants.MQOO_OUTPUT; // Open queue to perform MQPUTs
				}

				// Now specify the queue that we wish to open, and the open
				// options...
				requestQueue = qm.accessQueue(queueNameSend.getId(), openOptions, null, // default q manager
						null, // no dynamic q name
						null); // no alternate user id

				// Create new MQMessage object
				sendMessage = new MQMessage();
			} catch (NullPointerException e) {
				LOG.error("Null Pointer error while preparing send queue", e);
				e.printStackTrace();
			}

			sendMessage.format = MQConstants.MQFMT_STRING; // Set message format
															// to
															// MQC.MQFMT_STRING
															// for use without
															// MQCIH header

			// NB. Change to 'MQCICS ' if using header !!!
			//			sendMessage.characterSet = 1208; // UTF-8

			// String str = "AMQ!NEW_SESSION_CORRELID";
			// byte byteArray[] = str.getBytes();
			// sendMessage.correlationId = byteArray;//str;

			// Set request type
			sendMessage.messageType = MQConstants.MQMT_REQUEST;
			//			sendMessage.messageType = MQConstants.MQMT_DATAGRAM;

			// Set reply queue
			if(hasReply)
				sendMessage.replyToQueueName = queueNameReceive.getId();

			// Set message text
			// String buffer = new String(bufferFront + messageText +
			// bufferEnd);
			String buffer = messageDescriptor.getText();
			sendMessage.writeString(buffer);
			
			// set the message headers
			MessageDescriptorHelper.setMessageHeaders(sendMessage, messageDescriptor);

			// Specify the message options...(default)
			MQPutMessageOptions pmo = new MQPutMessageOptions();
			pmo.options = MQConstants.MQPMO_ASYNC_RESPONSE | MQConstants.MQPMO_NEW_MSG_ID;

			// Put the message on the queue using default options
			try {
				requestQueue.put(sendMessage, pmo);
				requestQueue.close();
			} catch (NullPointerException e) {
				if (LOG.isErrorEnabled())
					LOG.error("Request Q is null - cannot put message");
			}
			if (LOG.isDebugEnabled())
				LOG.debug("Message placed on queue");

			//Put the sent message with updated headers from the broker to the request tab
			messageDescriptor.addHeader(HeaderDescriptor.HEADER_MESSAGE_ID, sendMessage.messageId == null ? "" : new String(sendMessage.messageId));
			GregorianCalendar putDateTime = sendMessage.putDateTime;
			messageDescriptor.addHeader(HeaderDescriptor.HEADER_PUT_DATETIME, putDateTime == null ? "" : putDateTime.getTime().toString());
			messageDescriptor.addHeader(HeaderDescriptor.HEADER_CORRELATION_ID, sendMessage.correlationId == null ? "" : new String(sendMessage.correlationId));

			presenter.setSentMessage(messageDescriptor);

			// Store the messageId for future use...
			// Define a MQMessage object to store the message ID as a
			// correlation ID
			// so we can retrieve the correct reply message later.
			MQMessage storedMessage = new MQMessage();

			// Copy current message ID across to the correlation ID
			storedMessage.correlationId = sendMessage.messageId;
			// storedMessage.characterSet = 1208; // UTF-8

			if (LOG.isDebugEnabled()) {
				LOG.debug("Message ID for sent message = '" + new String(sendMessage.messageId) + "'");
				LOG.debug("Correlation ID stored = '" + new String(storedMessage.correlationId) + "'");
			}

			if(hasReply) {
				
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
				WebsphereMessageReceivingTask task = new WebsphereMessageReceivingTask(handler, qm, queueNameReceive, brokerTimeout,
						storedMessage, queueNameSend);
				
				Thread responseReceivingThread = new Thread(task);
				responseReceivingThread.setDaemon(true);
				responseReceivingThread.start();
			}
			

		} catch (MQException ex) {
			if (LOG.isErrorEnabled())
				LOG.error(ex + ": " + PCFConstants.lookupReasonCode(ex.reasonCode));
			
			throw new CannotSendMessageException(ex.getMessage() + ". Reason code: " + PCFConstants.lookupReasonCode(ex.reasonCode), ex);
			
		} catch (java.io.IOException ex) {
			if (LOG.isErrorEnabled())
				LOG.error(ex.getMessage());
			throw new CannotSendMessageException(ex.getMessage(), ex);
		} catch (Exception ex) {
			if (LOG.isErrorEnabled())
				LOG.error(ex.getMessage());
			throw new CannotSendMessageException(ex.getMessage(), ex);
		}
	}

}
